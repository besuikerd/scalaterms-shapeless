package org.metaborg.scalaterms.poly

import org.metaborg.scalaterms.{Origin, STerm}
import shapeless.ops.{coproduct, hlist, tuple}
import shapeless._

object PolyToSTerm{
  def apply[T, R](t: T)(
    implicit cse: polyToSTerm.Case.Aux[T, R]
  ): R = cse(t)

  import org.metaborg.scalaterms.STerm.{Cons => SCons, Int => SInt, List => SList, Real => SReal, String => SString, Tuple => STuple}

  trait lowPriorityPolyToSTerm extends Poly1 {
    implicit def caseProduct[T <: Product, Repr <: HList, Mapped <: HList](
      implicit generic: Generic.Aux[T, Repr]
      , notTuple: IsNotTuple[T]
      , mapper: Lazy[hlist.Mapper.Aux[polyToSTerm.type, Repr, Mapped]]
      , toTraversable: hlist.ToTraversable.Aux[Mapped, Seq, STerm]
    ) = at[T].apply(p => {
      val children = toTraversable(mapper.value(generic.to(p)))
      SCons(p.productPrefix, children, None)
    })

    implicit def caseProductWithOrigin[T <: Product, Repr <: HList, Init <: HList, Mapped <: HList] (
      implicit generic: Generic.Aux[T, Repr]
      , notTuple: IsNotTuple[T]
      , last: hlist.Last.Aux[Repr, Origin]
      , init: hlist.Init.Aux[Repr, Init]
      , mapper: Lazy[hlist.Mapper.Aux[polyToSTerm.type, Init, Mapped]]
      , toTraversable: hlist.ToTraversable.Aux[Mapped, Seq, STerm]
    ) = at[T].apply(p => {
      val children = generic.to(p)
      SCons(p.productPrefix, toTraversable(mapper.value(init(children))), Some(last(children)))
    })

    implicit def caseCoProduct[T, Repr <: Coproduct](
      implicit generic: Generic.Aux[T, Repr]
      , folder: Lazy[coproduct.Folder.Aux[polyToSTerm.type, Repr, STerm]]
    ) = at[T].apply(co => {
      folder.value(generic.to(co))
    })

    implicit def caseTuple[T, Mapped](
      implicit
      isTuple: IsTuple[T],
      mapper: Lazy[tuple.Mapper.Aux[T, polyToSTerm.type, Mapped]],
      toList: tuple.ToTraversable.Aux[Mapped, Seq, STerm]
    ) = at[T].apply { tpl =>
      STuple(toList(mapper.value(tpl)), None)
    }
  }

  object polyToSTerm extends lowPriorityPolyToSTerm {

    implicit def caseSTerm[T <: STerm] = at[T](identity)

    implicit def caseInt = at[Int].apply(SInt.apply(_, None))

    implicit def caseString = at[String].apply(SString.apply(_, None))

    implicit def caseFloat = at[Float].apply(SReal.apply(_, None))

    implicit def caseDouble = at[Double].apply(SReal.apply(_, None))

    implicit def caseBoolean = at[Boolean].apply(b => SCons(if (b) "True" else "False", List(), None))

    implicit def caseTraversable[U, T[U] <: Traversable[U]](
      implicit cse: Case.Aux[U, STerm]
    ) = at[T[U]](t => SList(t.map(cse).to[Seq], None))
  }

  implicit class ImplicitToSTerm[T, R](t: T)(implicit cse: polyToSTerm.Case.Aux[T, R]){
    def toSTerm: R = cse(t)
  }
}

