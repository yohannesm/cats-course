package part2abstractMath

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

object MonadTransformersExamples {
  import cats.data.OptionT
  import cats.instances.list._ // fetch an implicit OptionT[List]

  def sumAllOptions(values: List[Option[Int]]): Int = ???

  // option transformer
  import cats.instances.future._

  val listOfNumberOptions: OptionT[List, Int] = OptionT(List(Option(1), Option(2)))
  val listOfCharOptions: OptionT[List, Char]  = OptionT(List(Option('a'), Option('b'), Option.empty[Char]))
  val listOfTuples: OptionT[List, (Int, Char)] = for {
    char   <- listOfCharOptions
    number <- listOfNumberOptions
  } yield (number, char)

  // either transformer
  import cats.data.EitherT
  val listOfEithers: EitherT[List, String, Int]    = EitherT(List(Left("something wrong"), Right(43), Right(2)))
  implicit val ec: ExecutionContext                = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  val futureOfEither: EitherT[Future, String, Int] = EitherT.right(Future(45)) // wrap over Future(Right(45))

  /*
    TODO exercise
    We have a multi-machine cluster for your business which will receive a traffic surge following a media appearance.
    We measure bandwidth in units.
    We want to allocate TWO of our servers to cope with the traffic spike.
    We know the current capacity for each server and we know we'll hold the traffic if the sum of bandwidths is > 250.
   */
  val bandwidths = Map(
    "server1.rockthejvm.com" -> 50,
    "server2.rockthejvm.com" -> 300,
    "server3.rockthejvm.com" -> 170
  )
  type AsyncResponse[T] = EitherT[Future, String, T] // wrapper over Future[Either[String, T]]

  def getBandwidth(server: String): AsyncResponse[Int] = bandwidths.get(server) match {
    case None    => EitherT.left(Future(s"Server $server unreachable"))
    case Some(b) => EitherT.right(Future(b))
  }

  // TODO 1
  // hint: call getBandwidth twice, and combine the results
  val bandwidthThreshold = 250

  def canWithstandSurge(s1: String, s2: String): AsyncResponse[Boolean] = for {
    b1 <- getBandwidth(s1)
    b2 <- getBandwidth(s2)
  } yield b1 + b2 > bandwidthThreshold
  // Future[Either[String, Boolean]]

  // TODO 2
  // hint: call canWithstandSurge + transform
  def generateTrafficSpikeReport(s1: String, s2: String): AsyncResponse[String] =
    canWithstandSurge(s1, s2).map { canWithstandSurge =>
      if (canWithstandSurge) "two servers can deal with the surge"
      else "two servers CANNOT withstand the surge"
    }

  //  ^^^^^^^^^^^^^^^               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  // Future[Either[String, Boolean]] ---- Future[Either[String, String]]

  def main(args: Array[String]): Unit = {
    println(listOfTuples.value)
    val resultFuture  = generateTrafficSpikeReport("server2.rockthejvm.com", "server3.rockthejvm.com").value
    val resultFuture1 = generateTrafficSpikeReport("server1.rockthejvm.com", "server3.rockthejvm.com").value
    val resultFuture2 = generateTrafficSpikeReport("server2.rockthejvm.com", "server4.rockthejvm.com").value
    resultFuture.foreach(println)
    resultFuture1.foreach(println)
//    resultFuture2.foreach(println)
  }

}
