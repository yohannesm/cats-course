package part2abstractMath

import cats.Monoid

object Monoids {

  import cats.Semigroup
  import cats.instances.int._
//  import cats.syntax.semigroup._ // |+|
  import cats.syntax.monoid._
  val numbers = (1 to 1000).toList
  //|+| is always associative
  val sumLeft  = numbers.foldLeft(0)(_ |+| _)
  val sumRight = numbers.foldRight(0)(_ |+| _)

  //define a general API
//  def combineFold[T: Semigroup](list: List[T]): T =
//    list.foldLeft( /* what?! */ )(_ |+| _)

  //MONOIDS
  import cats.Monoid
  val intMonoid  = Monoid[Int]
  val combineInt = intMonoid.combine(25, 999)
  val zero       = intMonoid.empty

  import cats.instances.string._ // bring the implicit Monoid[String] in scope
  val emptyString   = Monoid[String].empty // ""
  def combineString = Monoid[String].combine("I understand", "Monoids")

  import cats.instances.option._ //construct an implicit Monoids[Option[Int]]
  val emptyOption    = Monoid[Option[Int]].empty
  val combineOption  = Monoid[Option[Int]].combine(Option(2), Option.empty[Int])
  val combineOption2 = Monoid[Option[Int]].combine(Option(3), Option(6))

  //extension methods for Monoids - |+|
  val combinedOptionFancy = Option(3) |+| Option(7)

  //TODO 1: implement combineFold
  def combineFold[T: Monoid](list: List[T]): T =
    list.foldLeft(Monoid[T].empty)(_ |+| _)

  //TODO 2: combine a list of phonebooks as Maps[String, Int]
  //hint: don't construct a monoid yourself. use an import
  import cats.instances.map._
  val phonebooks = List(
    Map(
      "Alice" -> 235,
      "Bob"   -> 647
    ),
    Map(
      "Charlie" -> 372,
      "Daniel"  -> 889
    ),
    Map(
      "Tina" -> 123
    )
  )

  //TODO 3 - shopping cart and online stores with Monoids
  //hint define your Monoid
  case class ShoppingCart(items: List[String], total: Double)

  import cats.instances.list._
//  implicit val monoidShoppingCartInstance = Monoid.instance[ShoppingCart](
//    (ShoppingCart(List.empty[String], 0.0)),
//    (sc1, sc2) => ShoppingCart(sc1.items |+| sc2.items, sc1.total + sc2.total)
//  )

  implicit def monoidSC: Monoid[ShoppingCart] = new Monoid[ShoppingCart] {
    override def empty: ShoppingCart = ShoppingCart(List(), 0.0)

    override def combine(x: ShoppingCart, y: ShoppingCart): ShoppingCart =
      ShoppingCart(x.items ++ y.items, x.total + y.total)
  }

//  implicit val shoppingCartMonoid: Monoid[ShoppingCart] = Monoid.instance[ShoppingCart](
//    ShoppingCart(List(), 0.0),
//    (sa, sb) => ShoppingCart(sa.items ++ sb.items, sa.total + sb.total)
//  )
  def checkout(shoppingCarts: List[ShoppingCart]): ShoppingCart =
    combineFold(shoppingCarts)

  def main(args: Array[String]): Unit = {
//    println(sumLeft)
//    println(sumRight)
//    println(combineOption)
//    println(combineFold(numbers))
//    println(combineFold(List("I ", "like ", "monoids.")))
    println(combineFold(phonebooks))
    println(
      checkout(
        List(
          ShoppingCart(List("iphone", "shoes"), 799),
          ShoppingCart(List("TV", "shoes"), 20000),
          ShoppingCart(List(), 0)
        )
      )
    )
  }

}
