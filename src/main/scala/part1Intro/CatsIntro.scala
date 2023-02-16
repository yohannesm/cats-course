package part1Intro

object CatsIntro {

  //part1 - tyoe class import
  import cats.Eq
  //Eq
  val aComparison = 2 == "a string"

  //part2 import TC instances for the types you need
  import cats.instances.int._

  //part 3 - use the typeclass API
  val intEquality         = Eq[Int]
  val aTypeSafeComparison = intEquality.eqv(12, 32) //false
  //val anUnsafeComparison  = intEquality.eqv(12, "32") //false

  //part 4 - use extension method if applicable
  import cats.syntax.eq._
  val anotherTypeSafeComp = 2 === 3
  val neqComparison       = 2 =!= 3
  //val invalidComparison = 2 === "aString"

  //part 5 - extending the TC operations to composite types, e.g. Lists
  //extension methods are only visible in the presence of the right TC instance
  import cats.instances.list._ // we bring Eq[List[Int]] in scope

  val aListComparison = List(2) === List(3)

  //part 6
  case class ToyCar(model: String, price: Double)
  implicit val toyCarEq: Eq[ToyCar] = Eq.instance[ToyCar] { (car1, car2) =>
    car1.price == car2.price
  }

  val compareTwoToyCars = ToyCar("Ferrari", 29.99) === ToyCar("Lamborghini", 29.99)

}
