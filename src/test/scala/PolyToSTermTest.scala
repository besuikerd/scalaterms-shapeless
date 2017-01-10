import org.metaborg.scalaterms.Origin
import org.metaborg.scalaterms.STerm.{Cons => SCons, Int => SInt, String => SString, Tuple => STuple}
import org.scalatest.FlatSpec
import shapeless.test._

class PolyToSTermTest extends FlatSpec {
  import org.metaborg.scalaterms.poly.PolyToSTerm

  "recursive data structures" should "be convertable to STerms" in {
    sealed trait Num
    case class Zero() extends Num
    case class Succ(n: Num) extends Num

    val two = Succ(Succ(Zero()))
    val sTwo = PolyToSTerm(two)
    assertResult(sTwo)(
      SCons("Succ", Seq(
        SCons("Succ", Seq(
          SCons("Zero", Seq.empty, None)
        ), None)
      ), None)
    )
  }

  "tuples" should "convert to STuple" in {
    val tpl = ("John", 42, true)
    val sTpl = PolyToSTerm(tpl)
    typed[STuple](sTpl)
    assertResult(sTpl.value(0))(SString(tpl._1, None))
    assertResult(sTpl.value(1))(SInt(tpl._2, None))
    assertResult(sTpl.value(2))(SCons("True", Seq.empty, None))
  }

  "Case classes with origin" should "extract their origin" in {
    case class WithOrigin(s: String, i: Int, o: Origin)

    val origin = new Origin("dummy", 42, 42, 42, 42)
    val obj = WithOrigin("John", 42, origin)
    val sObj = PolyToSTerm(obj)
    assertResult(sObj)(
      SCons("WithOrigin", Seq(
        SString("John", None),
        SInt(42, None)
      ), Some(origin))
    )
  }

  "implicit ToSTerm" should "implicitly have the toSTerm method on convertable objects" in {
    import PolyToSTerm.ImplicitToSTerm

    assertResult(STuple(Seq(SInt(2, None), SInt(3, None)), None))((2,3).toSTerm)

    class Incompatible(s:String)

    assertTypeError("""new Incompatible("dsa").toSTerm""")
  }
}
