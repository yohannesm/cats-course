package part2abstractMath

object Semigroups {

  //Semigroups combine elements of the same type
  import cats.Semigroup
  import cats.instances.int._

  val naturalIntSemigroup = Semigroup[Int]
  val intCombination      = naturalIntSemigroup.combine(2, 46)
  import cats.instances.string._

  val naturalStringSemigroup = Semigroup[String]
  val stringCombination      = naturalStringSemigroup.combine("I love ", "Cats")

  //specific API
  def reduceInts(list: List[Int]): Int          = list.reduce(naturalIntSemigroup.combine)
  def reduceStrings(list: List[String]): String = list.reduce(naturalStringSemigroup.combine)

  //general API
  def reduceThings[T](list: List[T])(implicit semigroup: Semigroup[T]): T = list.reduce(semigroup.combine)
//  implicit val toyCarEq: Eq[ToyCar] = Eq.instance[ToyCar] { (car1, car2) =>
//    car1.price == car2.price
//  }

  //TODO 1: support a new type for a semigroup
  //hint: use the same pattern we used with Eq
  case class Expense(id: Long, amount: Double)
  implicit val expenseSemigroup: Semigroup[Expense] = Semigroup.instance[Expense] { (e1, e2) =>
    Expense(Math.max(e1.id, e2.id), e1.amount + e2.amount)
  }

  //extension methods from Semigroup - |+|
  import cats.syntax.semigroup._
  val anIntSum         = 2 |+| 3 // requires the presence of an implicit Semigroup[Int]
  val aStringConcat    = "we like " |+| "semigroups."
  val aCombinedExpense = Expense(4, 80) |+| Expense(56, 46)

  //TODO 2: implement reduceThings2 with the |+|
  def reduceThings2[T: Semigroup](list: List[T]): T = list.reduce(_ |+| _)

  def main(args: Array[String]): Unit = {
    println(intCombination)
    println(stringCombination)

    val numbers = (1 to 10).toList
    println(reduceInts(numbers))
    val strings = List("I'm ", " starting", " to", " like", " semigroups")
    println(reduceStrings(strings))

    //compiler injects the implicit Semigroup[Int]
    println(reduceThings(numbers))
    //compiler injects the implicit Semigroup[String]
    println(reduceThings(strings))
    import cats.instances.option._ //compiler will produce an implicit Semigroup[Option[Int]] - combine will produce
    //another option with the summed elements

    val numberOptions = numbers.map(n => Option(n))
    println(reduceThings(numberOptions)) //an Option[Int] containing the sum of all the numbers
    val stringOptions = strings.map(s => Option(s))
    println(reduceThings(stringOptions))

    //test ex 1
    val expenses = List(Expense(1, 99.0), Expense(2, 35.0), Expense(43, 10.0))
    println(reduceThings(expenses))

    //test ex 2
    println(reduceThings2(expenses))

  }

}
