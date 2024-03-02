package part2abstractMath

import cats.syntax.monad

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object Monads {

  //lists
  val numbersList = List(1, 2, 3)
  val charsList   = List('a', 'b', 'c')
  //TODO 1.1: how do you create all combinations of (number, char)
  val combinationsListFor = for {
    i <- numbersList
    c <- charsList
  } yield (i, c) //identical

  //options
  val numberOption = Option(2)
  val charOption   = Option('d')
  //TODO 1.2: how do you create the combination of (number, char)
  val combinationOption = numberOption.flatMap(n => charOption.map(c => (n, c)))
  val combinationOptionFor = for {
    n <- numberOption
    c <- charOption
  } yield (n, c)

  //futures
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  val numberFuture                  = Future(42)
  val charFuture                    = Future('Z')

  //TODO 1.3: how do you create the combination of (number, char)?
  val combinationFuture = numberFuture.flatMap(n => charFuture.map(c => (n, c)))
  val combinationFutureFor = for {
    n <- numberFuture
    c <- charFuture
  } yield (n, c)

  /*
   * Pattern
   * - wrapping a value into a monadic value
   * - the flatMap mechanism
   *
   * Monads
   * */
  trait MyMonad[M[_]] {
    def pure[A](value: A): M[A]
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
    def map[A, B](ma: M[A])(f: A => B): M[B] = {
      flatMap(ma)(a => pure(f(a)))
    }
  }

  //Cats Monad
  import cats.Monad
  import cats.instances.option._ //implicit Monad[Option]
  val optionMonad        = Monad[Option]
  val anOption           = optionMonad.pure(4)
  val aTransformedOption = optionMonad.flatMap(anOption)(x => if (x % 3 == 0) Option(x + 1) else None)

  import cats.instances.list._
  val listMonad        = Monad[List]
  val aList            = listMonad.pure(3)
  val aTransformedList = listMonad.flatMap(aList)(x => List(x, x + 1))

  //TODO 2: use a Monad[Future]
  import cats.instances.future._
  val futureMonad        = Monad[Future] //requires an implicit execution context
  val aFuture            = futureMonad.pure(13)
  val aTransformedFuture = futureMonad.flatMap(aFuture)(x => Future(x + 22))

  //specialized API
  def getPairsList(numbers: List[Int], chars: List[Char]): List[(Int, Char)] =
    numbers.flatMap(n => chars.map(c => (n, c)))
  def getPairsOption(number: Option[Int], char: Option[Char]): Option[(Int, Char)] = {
    number.flatMap(n => char.map(c => (n, c)))
  }
  def getPairsFutures(numbers: Future[Int], char: Future[Char]): Future[(Int, Char)] = {
    numbers.flatMap(n => char.map(c => (n, c)))
  }

  //generalize
  def getPairs[M[_], A, B](ma: M[A], mb: M[B])(implicit monad: Monad[M]): M[(A, B)] =
    monad.flatMap(ma)(a => monad.map(mb)(b => (a, b)))

  //extension methods - weirder imports - pure, flatMap
  import cats.syntax.applicative._ // pure is here
  val oneOption = 1.pure[Option] // implicit Monad[Option] will be used => Some(1)
  val oneList   = 1.pure[List]   // List(1)

  import cats.syntax.flatMap._ // flatMap is here
  val oneOptionTransformed = oneOption.flatMap(x => (x + 1).pure[Option])

  // TODO 3: implement the map method in MyMonad
  // Monads extend Functors
  import cats.syntax.functor._ // map is here
  val oneOptionMapped  = Monad[Option].map(Option(2))(_ + 1)
  val oneOptionMapped2 = oneOption.map(_ + 2)

  //for-comprehensions
  val composedOptionFor = for {
    one <- 1.pure[Option]
    two <- 2.pure[Option]
  } yield one + two

  //TODO 4: implement a shorter version of getPairs using for-comprehension.
  def getPairsFor[M[_]: Monad, A, B](ma: M[A], mb: M[B]): M[(A, B)] = {
    for {
      a <- ma
      b <- mb
    } yield (a, b)
  }

  trait MyMonad2[M[_]] {
    def pure[A](value: A): M[A]
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
    def map[A, B](ma: M[A])(f: A => B): M[B] =
      flatMap(ma)(a => pure(f(a)))
  }

  def main(args: Array[String]): Unit = {
    println(numbersList.flatMap(i => charsList.map(c => (i, c))))
    println(aTransformedOption)
    println(getPairs(numbersList, charsList))
    println(getPairs(numberOption, charOption))
    getPairs(numberFuture, charFuture).foreach(println)
  }
}
