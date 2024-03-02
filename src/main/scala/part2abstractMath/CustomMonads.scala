package part2abstractMath

import scala.annotation.tailrec

object CustomMonads {

  import cats.Monad
  implicit object OptionMonad extends Monad[Option] {
    override def pure[A](x: A): Option[A] = Option(x)

    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa.flatMap(f)

    //ensure tailRecM does NOT stack-overflow and has a base-case or termination/ending condition
    //in short tailRecM will need to keep applying f to its value if it's still an A until
    //it becomes a B. Then the B is the final value that we want.
    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] = f(a) match {
      case None           => None
      case Some(Left(v))  => tailRecM(v)(f)
      case Some(Right(b)) => Some(b)
    }
  }

  //TODO 1: define a monad for the identity type
  type Identity[T] = T
  val aNumber: Identity[Int] = 42

  implicit object IdentityMonad extends Monad[Identity] {
    override def pure[A](x: A): Identity[A] = x

    override def flatMap[A, B](a: Identity[A])(f: A => Identity[B]): Identity[B] = f(a)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Identity[Either[A, B]]): Identity[B] = f(a) match {
      case Left(v)  => tailRecM(v)(f)
      case Right(b) => b
    }
  }

  //harder example
  sealed trait Tree[+A]
  final case class Leaf[+A](value: A)                        extends Tree[A]
  final case class Branch[+A](left: Tree[A], right: Tree[A]) extends Tree[A]

  //TODO 2: define a monad for this tree
  //tailrecM tailrec is difficult

  implicit object TreeMonad extends Monad[Tree] {
    override def pure[A](x: A): Tree[A] = Leaf(x)

    override def flatMap[A, B](a: Tree[A])(f: A => Tree[B]): Tree[B] = a match {
      case Leaf(value)         => f(value)
      case Branch(left, right) => Branch(flatMap(left)(f), flatMap(right)(f))
    }

    override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = {
      def stackRec(t: Tree[Either[A, B]]): Tree[B] = t match {
        case Leaf(Left(v))       => stackRec(f(v))
        case Leaf(Right(b))      => Leaf(b)
        case Branch(left, right) => Branch(stackRec(left), stackRec(right))
      }
      stackRec(f(a))
    }
  }

  def main(args: Array[String]): Unit = {
    val tree: Tree[Int] = Branch(Leaf(10), Leaf(20))
    val changedTree     = TreeMonad.flatMap(tree)(v => Branch(Leaf(v + 1), Leaf(v + 2)))
    println(changedTree)
  }
}
