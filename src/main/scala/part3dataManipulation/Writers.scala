package part3dataManipulation

import cats.Id
import cats.data.WriterT

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object Writers {

  import cats.data.Writer
  //left hand side is a Log, right hand side is Value
  //Writer[L, V]
  //1. define them at the start
  val aWriter: Writer[List[String], Int] = Writer(List("Started something"), 45)
  // 2. manipulate them with pure FP
  val anIncreasedWriter: WriterT[Id, List[String], Int] = aWriter.map(_ + 1) // value increases, logs stay the same
  val aLogsWriter: WriterT[Id, List[String], Int] =
    aWriter.mapWritten(_ :+ "found something interesting") // value stays the same, logs change.
  val aWriterWithBoth = aWriter.bimap(_ :+ "found something else", _ + 1) // both changes
  val aWriterWithBoth2 = aWriter.mapBoth { (logs, value) =>
    (logs :+ "found something interesting", value + 1)
  }

  //flatMap on writers
  import cats.instances.vector._ //imports a Semigroup[Vector]
  val writerA: Writer[Vector[String], Int] = Writer(Vector("Log A1", "Log A2"), 10)
  val writerB: Writer[Vector[String], Int] = Writer(Vector("Log B1"), 40)
  val compositeWriter: Writer[Vector[String], Int] = for {
    va <- writerA
    vb <- writerB
  } yield va + vb

  //reset the logs
  import cats.instances.list._ // import an implicit Monoid[List[Int]]
  val anEmptyWriter: WriterT[Id, List[String], Int] = aWriter.reset // clear the logs and keep the desired value

  //3. dump either the value or the logs
  val desiredValue: Id[Int]  = aWriter.value
  val logs: Id[List[String]] = aWriter.written
  val (l, v)                 = aWriter.run

  //TODO 1 : rewrite a function which "prints" things with writers
  def countAndSay(n: Int): Unit = {
    if (n <= 0) println("starting!")
    else {
      countAndSay(n - 1)
      println(n)
    }
  }

  def countAndLog(n: Int): Writer[Vector[String], Int] = {
    import scala.annotation.tailrec
    @tailrec
    def kickOff(n: Int, curVal: Writer[Vector[String], Int]): Writer[Vector[String], Int] = {
      if (n <= 0) curVal.mapWritten(_ :+ "starting!")
      else {
        val nextVal: Writer[Vector[String], Int] = Writer(Vector(s"${n}"), n)
        val newWriter: Writer[Vector[String], Int] = for {
          ca <- curVal
          na <- nextVal
        } yield ca + na
        kickOff(n - 1, newWriter)
      }
    }

    kickOff(n, Writer(Vector[String](), 0))
  }

  //Benefit $1: we work with pure FP
  //TODO 2: rewrite this method with writers
  def naiveSum(n: Int): Int = {
    if (n <= 0) 0
    else {
      println(s"Now at $n")
      val lowerSum = naiveSum(n - 1)
      println(s"computed sum(${n - 1}) = $lowerSum")
      lowerSum + n
    }
  }

//  def sumWithLogs(n: Int): Writer[Vector[String], Int] = {
//    if (n <= 0) Writer[Vector[String], Int](Vector.empty, 0)
//    else {
//      val nextWriter = for {
//        nextVal <- sumWithLogs(n - 1).value
//      } yield nextVal
//    }
//  }

  def countAndLog2(n: Int): Writer[Vector[String], Int] = {
    //    import scala.annotation.tailrec
    //    @tailrec
    //    def kickOff(n: Int, curVal: Writer[Vector[String], Int]): Writer[Vector[String], Int] = {
    //      if (n <= 0) curVal
    //      else {
    //        val nextVal: Writer[Vector[String], Int] = Writer(Vector[String](s"${n}"), n)
    //        val newWriter: Writer[Vector[String], Int] = for {
    //          ca <- curVal
    //          na <- nextVal
    //        } ca + na
    //        kickOff(n - 1, newWriter)
    //      }
    //    }

    //    kickOff(n, Writer(Vector[String](), 0))
    if (n <= 0) Writer(Vector("starting!"), 0)
    else countAndLog2(n - 1).flatMap(_ => Writer(Vector(s"$n"), n))
  }

  def sumWithLogs2(n: Int): Writer[Vector[String], Int] =
    if (n <= 0) Writer(Vector(), 0)
    else
      for {
        _        <- Writer(Vector(s"Now at $n"), n)
        lowerSum <- sumWithLogs2(n - 1)
        _        <- Writer(Vector(s"Computed sum${n - 1} = $lowerSum"), n)
      } yield lowerSum + n

  def main(args: Array[String]): Unit = {
    println(compositeWriter.run)
    //ex 1
//    countAndSay(10)
    countAndLog2(10).written.foreach(println)
    //ex 2
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
//    Future(naiveSum(100)).foreach(println)
//    sumWithLogs(100).written.foreach(println)
//    val sumFuture1                    = Future(sumWithLogs(100))
//    val sumFuture2                    = Future(sumWithLogs(100))
//    val logs1: Future[Vector[String]] = sumFuture1.map(_.written)
//    val logs2: Future[Vector[String]] = sumFuture2.map(_.written)
//    logs1.foreach(println)
//    logs2.foreach(println)
  }
}
