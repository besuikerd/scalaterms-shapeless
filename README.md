# Scalaterms-shapeless

Allows type-safe and automatic conversion of scala types to `STerm`.

The following types can be converted to `STerm`:

* (most) Primitive types
* Tuples
* Case classes
* Sumlike types (sealed traits)
* `Product` instances

If a product has an `Origin` as its last argument, this argument is used to track the origin of this `Cons`.

To convert an object, simply call `PolyToSTerm.apply`:

```scala
val tpl: Tuple = PolyToSTerm((2,3)) // Tuple(Seq(Int(2, None), Int(3, None), None)

sealed trait Tree
case class Leaf(n: Int) extends Tree
case class Node(l: Tree, r: Tree) extends Tree

val tree = Node(
  Node(
    Leaf(2),
    Leaf(3)
  ),
  Node(
    Leaf(4),
    Leaf(5)
  )
)

val sTree: Cons = PolyToSTerm(tree)

case class WithOrigin(s: String, o: Origin)

val wo = WithOrigin("hasOrigin", new Origin("dummy origin", 42, 42, 42, 42))
PolyToSTerm(wo) // Cons("WithOrigin", Seq(String("hasOrigin", None)))
```

If you pass it a value that cannot be converted to a `STerm`, it throws a type error:

```scala
class Incompatible(s: String)

val inc = new Incompatible("not compatible")
PolyToSTerm(inc) // type error
```

There is also an implicit version to convert objects to `STerm`:

```scala
import org.metaborg.scalaterms.poly.PolyToSTerm.ImplicitToSTerm

(2,3).toSTerm
```