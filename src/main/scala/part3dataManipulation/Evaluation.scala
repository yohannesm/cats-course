package part3dataManipulation

object Evaluation {
  /*
    Cats make the distinction between
    - evaluating an expression eagerly (now)
    - evaluating lazily and every time you request it. (always)
    - evaluating lazily and keeping the value (memoizing). (later)
   */

  import cats.Eval
  val instantEval: Eval[Int] = Eval.now {
    println("Computing now!")
    64345
  }

  val redoEval = Eval.always {
    println("Computing again!")
    4234
  }

  val delayedEval = Eval.later {
    println("Computing later!")
    53278
  }

  val composedEvaluation = instantEval.flatMap(value1 => delayedEval.map(value2 => value1 + value2))
  val anotherComposedEvaluation = for {
    value1 <- instantEval
    value2 <- delayedEval
  } yield value1 + value2

  //TODO 1: predict the output
  val evalEx1 = for {
    a <- delayedEval
    b <- redoEval
    c <- instantEval
    d <- redoEval
  } yield a + b + c + d
  //[#1] now, later, again, again. (sum) [#2] again again (sum)

  // "remember" a computed value
  val dontRecompute = redoEval.memoize

  val tutorial = Eval
    .always { println("Step 1..."); "put the guitar on your lap" }
    .map { step1 => println("Step 2"); s"$step1 then put your left hand on the neck" }
    .memoize // remember the value up to this point
    .map { steps12 => println("Step 3, more complicated"); s"$steps12 then with the right hand strike the strings" }

  //TODO 2: implement defer such that defer(Eval.now) does NOT run the side effects
  def defer[T](eval: => Eval[T]): Eval[T] = Eval.later(()).flatMap(_ => eval)

  //TODO 3: rewrite the method with Evals
  def reverseList[T](list: List[T]): List[T] =
    if (list.isEmpty) list
    else reverseList(list.tail) :+ list.head

  def reverseEval[T](list: List[T]): Eval[List[T]] =
    if (list.isEmpty) Eval.now(list)
    else reverseEval(list.tail).map(_ :+ list.head)

  def main(args: Array[String]): Unit = {
//    println(instantEval)
//    println(redoEval.value)
//    println(delayedEval.value)
//    println(composedEvaluation.value)

//    println(evalEx1.value)
//    println(evalEx1.value)
//    println(dontRecompute.value)
//    println(dontRecompute.value)

//    println(tutorial.value)
//    println(tutorial.value)
//    println(defer(Eval.now {
//      println("Now!")
//      42
//    }).value)

    println(reverseEval((1 to 10).toList).value)
  }

}
