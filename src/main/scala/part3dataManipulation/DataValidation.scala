package part3dataManipulation

import cats.Semigroup
import cats.implicits.catsSyntaxSemigroup

import scala.annotation.tailrec
import scala.util.Try

object DataValidation {

  import cats.data.Validated
  val aValidValue: Validated[String, Int]    = Validated.valid(42) //"right" value
  val anInvalidValue: Validated[String, Int] = Validated.invalid("Something went wrong. ")
  val aTest: Validated[String, Int]          = Validated.cond(42 > 39, 99, "meaning of life is too small")

  /*
   - n must be a prime
   - n must be non negative
   - n <= 100
   - n must be even
   */
  def testPrime(n: Int): Boolean = {
    @tailrec
    def tailrecPrime(d: Int): Boolean =
      if (d <= 1) true
      else n % d != 0 && tailrecPrime(d - 1)

    if (n == 0 || n == 1 || n == -1) false
    else tailrecPrime(Math.abs(n / 2))
  }
  //TODO: use Either
  def testNumber(n: Int): Either[List[String], Int] = {
    val isNotEven: List[String]  = if (n % 2 == 0) List() else List("Number must be even")
    val isNegative: List[String] = if (n >= 0) List() else List("Number must be non negative")
    val isTooBig: List[String]   = if (n <= 100) List() else List("Number must be less than or equal to 100")
    val isNotPrime: List[String] = if (testPrime(n)) List() else List("Number must be a prime")

    if (n % 2 == 0 && n >= 0 && n < 100 && testPrime(n)) Right(n)
    else Left(isNotEven ++ isNegative ++ isTooBig ++ isNotPrime)
  }

  import cats.instances.list._
  implicit val combineIntMax: Semigroup[Int] = Semigroup.instance[Int](Math.max)
  def validateNumber(n: Int): Validated[List[String], Int] =
    Validated
      .cond(n % 2 == 0, n, List("Number must be even"))
      .combine(Validated.cond(n >= 0, n, List("Number must be non-negative")))
      .combine(Validated.cond(n <= 100, n, List("Number must be big")))
      .combine(Validated.cond(testPrime(n), n, List("Number must be a prime")))

  //chain
  aValidValue.andThen(_ => anInvalidValue)
  //test a valid value
  aValidValue.ensure(List("something went wrong"))(_ % 2 == 0)
  //transform
  aValidValue.map(_ + 1)
  aValidValue.leftMap(_.length)
  aValidValue.bimap(_.length, _ + 1)
  //interoperate with stdlib
  val eitherToValidated: Validated[List[String], Int] = Validated.fromEither(Right(42))
  val optionToValidated: Validated[List[String], Int] = Validated.fromOption(None, List("nothing present here"))
  val tryToValidated: Validated[Throwable, Int]       = Validated.fromTry(Try("something".toInt))
  //backwards
  aValidValue.toOption
  aValidValue.toEither

  //TODO 2 - form validation
  object FormValidation {

    import cats.instances.string._

    type FormValidation[T] = Validated[List[String], T]

    /** fields are
      * - name
      * - email
      * - password
      *
      * rules are
      * - name , email and password MUST be specified
      *  - name must not be blank
      *  - email must have "@" character
      *  - password must have >= 10 characters.
      */
    def getValue(form: Map[String, String], fieldName: String): FormValidation[String] = {
      Validated.fromOption(form.get(fieldName), List(s"The $fieldName must not be empty"))
    }

    def nameNotBlank(form: Map[String, String]): FormValidation[String] = {
      getValue(form, "Name").andThen(name => Validated.cond(name.nonEmpty, name, List(s"Name should not be empty")))
    }

    def validEmail(form: Map[String, String]): FormValidation[String] = {
      getValue(form, "Email").andThen(email => Validated.cond(email.contains("@"), email, List(s"Email is invalid")))
    }

    def validPassword(form: Map[String, String]): FormValidation[String] = {
      getValue(form, "Password").andThen(password =>
        Validated.cond(password.length >= 10, password, List(s"Password is invalid"))
      )
    }

    import cats.syntax.validated._
    def validateForm(form: Map[String, String]): FormValidation[String] = {
      val combinedChecks: FormValidation[String] = nameNotBlank(form) |+| validEmail(form) |+| validPassword(form)
      combinedChecks.map(_ => "Registration Success")
    }
  }

  import cats.syntax.validated._
  val aValidMeaningOfLife: Validated[List[String], Int] = 42.valid[List[String]]
  val anError: Validated[List[String], Int]             = List("Something went wrong").invalid[Int]

  def main(args: Array[String]): Unit = {
    val form = Map(
      "Name"     -> "Daniel",
      "Email"    -> "daniel@rockthejvm.com",
      "Password" -> "Rockthejvm1!"
    )

    println(FormValidation.validateForm(form))
  }
}
