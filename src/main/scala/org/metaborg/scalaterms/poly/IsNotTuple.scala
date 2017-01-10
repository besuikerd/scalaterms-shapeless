package org.metaborg.scalaterms.poly
import shapeless.IsTuple
import shapeless.unexpected

trait IsNotTuple[T]
object IsNotTuple{
  implicit def fallback[T]: IsNotTuple[T] = new IsNotTuple[T]{}
  implicit def amb1[T](implicit ev: IsTuple[T]): IsNotTuple[T] = unexpected
  implicit def amb2[T](implicit ev: IsTuple[T]): IsNotTuple[T] = unexpected
  def apply[T](implicit ev: IsNotTuple[T]): IsNotTuple[T] = ev
}