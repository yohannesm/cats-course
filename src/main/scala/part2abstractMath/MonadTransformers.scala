//package part2abstractMath
//
//import cats.implicits.catsStdInstancesForFuture
//import cats.data._
//import cats.implicits._
//import cats.instances.future.catsStdInstancesForFuture
//
//import java.util.concurrent.Executors
//import scala.concurrent.{ExecutionContext, Future}
//import scala.concurrent.ExecutionContext.Implicits.global
//
//object MonadTransformers {
//
//  //option transformer
//  import cats.data.OptionT
//  import cats.instances.list._ // fetch an implicit OptionT[List]
////  def sumAllOptions(values: List[Option[Int]]): Int =
////    OptionT(values).foldLeft(0)((acc, curr) => acc + curr)
////
////  val listOfNumberOptions: OptionT[List, Int] = OptionT(List(Option(1), Option(2)))
////  val listOfCharOptions: OptionT[List, Char]  = OptionT(List(Option('a'), Option('b'), Option.empty[Char]))
////  val listOfTuples: OptionT[List, (Int, Char)] = for {
////    char   <- listOfCharOptions
////    number <- listOfNumberOptions
////  } yield (number, char)
//
//  //either transformer
//  import cats.data.EitherT
//  import cats.instances.future._
//  val listOfEithers: EitherT[List, String, Int] = EitherT(List(Left("something wrong"), Right(43), Right(2)))
//  implicit val ec: ExecutionContext             = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
////  val futureOfEither: EitherT[Future, String, Int] = EitherT.right(Future(45))
//
//  /*
//   * TODO Exercise
//   * We have a multi machine cluster for your business which will receive a traffic surge following a media appearance.
//   * We measure bandwidth in units.
//   * We want to allocate TWO of our servers to cope with the traffic spike.
//   * We know the current capacity for each server and we know we'll hold the traffic if the sum of the bandwidth is > 250
//   * */
//
//  val bandwidths = Map(
//    "server1.rockthejvm.com" -> 50,
//    "server2.rockthejvm.com" -> 300,
//    "server3.rockthejvm.com" -> 170
//  )
//
//  type AsyncResponse[T] = EitherT[Future, String, T]
//
//  def getBandwidth(server: String): AsyncResponse[Int] = bandwidths.get(server) match {
//    case Some(b) => EitherT.right(Future((b)))
//    case None    => EitherT.left(Future(s"Server $server unreachable"))
//  }
//  val maxBandwidth = 250;
//
//  //TODO ex1
//  //hint: call getBandwidth twice, and combine the results
//  def canWithstandSurge(s1: String, s2: String): AsyncResponse[Boolean] = for {
//    b1 <- getBandwidth(s1)
//    b2 <- getBandwidth(s2)
//  } yield (b1 + b2 > 250) //Future[Either[String, Boolean]]
//
//  //TODO ex2
//  // hint: call canWithstandSurge + transform
//  def generateTrafficSpikeReport(s1: String, s2: String): AsyncResponse[String] =
//    canWithstandSurge(s1, s2).transform {
//      case Left(reason) => Left(s"Servers $s1 and $s2 cannot cope with the incoming spike: $reason")
//      case Right(false) => Left(s"Servers $s1 and $s2 cannot cope with the incoming spike: not enough total bandwidth")
//      case Right(true)  => Right(s"Servers $s1 and $s2 can cope with the incoming spike no problem")
//    }
//
//  def main(args: Array[String]): Unit = {
//    println(listOfTuples.value)
//    val resultFuture: Future[Either[String, String]] =
//      generateTrafficSpikeReport("server2.rockthejvm.com", "server3.rockthejvm.com").value
//    resultFuture.foreach(println)
//    println(sumAllOptions(listOfNumberOptions.value))
//  }
//}
