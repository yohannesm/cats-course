package part2abstractMath

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object UsingMonads {

  import cats.Monad
  import cats.instances.list._
  import cats.instances.option._
  val monadList                = Monad[List]
  val aSimpleList              = monadList.pure(2) // List(2)
  val anExtendedList: Seq[Int] = monadList.flatMap(aSimpleList)(x => List(x, x + 1))
  //applicable to Option, Try, Future

  //either is also a monad
  val aManualEither: Either[String, Int] = Right(42)
  type LoadingOr[T] = Either[String, T]
  type ErrorOr[T]   = Either[Exception, T]
  import cats.instances.either._
  val loadingMonad = Monad[LoadingOr]
  val anEither     = loadingMonad.pure(45) // LoadingOr[Int] == Right(45)
  val aChangedLoading =
    loadingMonad.flatMap(anEither)(n => if (n % 2 == 0) Right(n + 1) else Left("loading meaning of life..."))

  //imaginary online store
  case class OrderStatus(orderId: Long, status: String)
  def getOrderStatus(orderId: Long): LoadingOr[OrderStatus] = Right(OrderStatus(orderId, "Ready to Ship"))

  def trackLocation(orderStatus: OrderStatus): LoadingOr[String] =
    if (orderStatus.orderId > 1000) Left("Not available yet, refreshing data...")
    else Right("Amsterdam, NL")

  val orderId = 457L
  val orderLocation: LoadingOr[String] =
    loadingMonad.flatMap(getOrderStatus(orderId))(orderStatus => trackLocation(orderStatus))
  //use extension methods
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  val orderLocationBetter: LoadingOr[String] = getOrderStatus(orderId).flatMap(os => trackLocation(os))
  val orderLocationFor: LoadingOr[String] = for {
    orderStatus <- getOrderStatus(orderId)
    location    <- trackLocation(orderStatus)
  } yield location

  //TODO : The service layer API of a web app
  case class Connection(host: String, port: String)
  val config = Map(
    "host" -> "localhost",
    "port" -> "4040"
  )

  trait HttpService[M[_]] {
    def getConnection(cfg: Map[String, String]): M[Connection]
    def issueRequest(connection: Connection, payload: String): M[String]
  }

  def getResponse[M[_]](service: HttpService[M], payload: String)(implicit monad: Monad[M]): M[String] =
    for {
      conn     <- service.getConnection(config)
      response <- service.issueRequest(conn, payload)
    } yield response
  //DO NOT CHANGE THE CODE

  /*
   *
  Requirements:
   * - if the host and port are found in the configuration map, then return an M containing a connection with those values
   * otherwise the method will fail, according to the logic of the type M
   * (for Try it will return a Failure, for Option it will return None, for Future it will be a failed Future,
   * for Either it will return a Left)
   * - the issueRequest method returns an M containing the string: "request (payload) has been accepted", if the payload is
   * less than 20 characters otherwise the method will fail, according to the logic of the type M.
   *
   * */

  // TODO: provide a real implementation of HttpService using Try, Option, Future, Either

  type ExceptionOr[T] = Either[Exception, T]
  implicit val eitherHttpService: HttpService[ErrorOr] = new HttpService[ErrorOr] {
    override def getConnection(cfg: Map[String, String]): ExceptionOr[Connection] = {
      val maybeConnection = for {
        host <- cfg.get("host")
        port <- cfg.get("port")
      } yield Connection(host, port)
      maybeConnection match {
        case Some(value) => Right(value)
        case None        => Left(new RuntimeException("Host or port was not found"))
      }
    }

    override def issueRequest(connection: Connection, payload: String): ExceptionOr[String] = {
      if (payload.length < 20) Right("request (payload) has been accepted")
      else Left(new RuntimeException("Request failed"))
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val optionHttpService: HttpService[Option] = new HttpService[Option] {
    override def getConnection(cfg: Map[String, String]): Option[Connection] = {
      if (cfg.contains("host") && cfg.contains("port")) {
        Option(Connection(cfg("host"), cfg("port")))
      } else None
    }

    override def issueRequest(connection: Connection, payload: String): Option[String] =
      if (payload.length < 20)
        Option(s"request ($payload) has been accepted")
      else None
  }

  implicit val tryHttpService: HttpService[Try] = new HttpService[Try] {
    override def getConnection(cfg: Map[String, String]): Try[Connection] = {
      if (cfg.contains("host") && cfg.contains("port")) {
        Success(Connection(cfg("host"), cfg("port")))
      } else Failure(new RuntimeException("failed to create connection"))
    }

    override def issueRequest(connection: Connection, payload: String): Try[String] =
      if (payload.length < 20)
        Success("request (payload) has been accepted")
      else Failure(new RuntimeException("failed to issue request"))
  }

  implicit val futureHttpService: HttpService[Future] = new HttpService[Future] {
    override def getConnection(cfg: Map[String, String]): Future[Connection] = {
      if (cfg.contains("host") && cfg.contains("port")) {
        Future.successful(Connection(cfg("host"), cfg("port")))
      } else Future.failed(new RuntimeException("failed to create connection"))
    }

    override def issueRequest(connection: Connection, payload: String): Future[String] =
      if (payload.length < 20)
        Future.successful("request (payload) has been accepted")
      else Future.failed(new RuntimeException("failed to issue request"))
  }

  implicit val eitherHttpService2: HttpService[ErrorOr] = new HttpService[ErrorOr] {
    override def getConnection(cfg: Map[String, String]): ErrorOr[Connection] = {
      if (cfg.contains("host") && cfg.contains("port")) {
        Right(Connection(cfg("host"), cfg("port")))
      } else Left(new RuntimeException("failed to create connection"))
    }

    override def issueRequest(connection: Connection, payload: String): ErrorOr[String] =
      if (payload.length < 20)
        Right(s"request ($payload) has been accepted")
      else Left(new RuntimeException("failed to issue request"))
  }

  def main(args: Array[String]): Unit = {
    val responseOption = optionHttpService
      .getConnection(config)
      .flatMap(conn => optionHttpService.issueRequest(conn, "Hello, HTTP service"))
    val responseOptionFor = for {
      conn     <- optionHttpService.getConnection(config)
      response <- optionHttpService.issueRequest(conn, "Hello, HTTP service")
    } yield response

    val errorOrResponse = for {
      conn     <- eitherHttpService.getConnection(config)
      response <- eitherHttpService.issueRequest(conn, "Hello ErrorOr")
    } yield response

    val errorOrResponse2 = for {
      conn     <- eitherHttpService2.getConnection(config)
      response <- eitherHttpService2.issueRequest(conn, "Hello ErrorOr")
    } yield response
    println(errorOrResponse2)

    println(responseOption)
    println(responseOptionFor)
    println(errorOrResponse)
    println(getResponse(optionHttpService, "Hello Option"))
    println(getResponse(eitherHttpService, "Hello Either"))
    println(getResponse(eitherHttpService2, "Hello Either"))
  }
}
