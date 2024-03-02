package part3dataManipulation

import cats.Id

object Readers {

  /*
   - configuration file => initial data structure
   - a DB layer
   - an Http layer
   - a business logic layer
   */
  case class Configuration(
      dbUsername: String,
      dbPassword: String,
      host: String,
      port: Int,
      nThreads: Int,
      emailReplyTo: String
  )
  case class DbConnection(username: String, password: String) {
    def getOrderStatus(orderId: Long): String = "dispatched"
    // select * from the db table and return the status of the orderIDa
    def getLastOrderId(username: String): Long = 542643L
    //select max(orderId) from table where username = username
  }

  case class HttpService(host: String, port: Int) {
    def start(): Unit = println("server started") // this would start the actual server
  }

  // bootstrap
  val config: Configuration = Configuration("daniel", "rockthejvm1!", "localhost", 1234, 8, "daniel@rockthejvm.com")
  //cats Reader
  import cats.data.Reader
  val dbReader: Reader[Configuration, DbConnection] =
    Reader(conf => DbConnection(conf.dbUsername, conf.dbPassword))
  val dbConn: Id[DbConnection] = dbReader.run(config)

  //Reader[I, O]
  val danielsOrderStatusReader: Reader[Configuration, String] =
    dbReader.map(dbcon => dbcon.getOrderStatus(55))
  val danielsOrderStatus: String = danielsOrderStatusReader.run(config)

  def getLastOrderStatus(username: String): String = {
    val usersLastOrderIdReader: Reader[Configuration, String] = dbReader
      .map(dbcon => dbcon.getLastOrderId(username))
      .flatMap(lastOrderId => dbReader.map(_.getOrderStatus(lastOrderId)))

    val usersOrderFor = for {
      lastOrderId <- dbReader.map(_.getLastOrderId(username))
      orderStatus <- dbReader.map(_.getOrderStatus(lastOrderId))
    } yield orderStatus

//    usersLastOrderIdReader.run(config)
    usersOrderFor.run(config)
  }
  /*
   * Pattern
   * 1. You create the initial data structure
   * 2. You create a reader which specifies how that data structure will be manipulated later
   * 3. you can then map and flatmap the reader to produce derived information
   * 4. when you need the final piece of information, you will call run on the reader with the initial data structure
   * */

  //TODO 1
  case class EmailService(emailReplyTo: String) {
    def sendEmail(address: String, contents: String) = s"From: $emailReplyTo; to $address >>> $contents"
  }

  val emailServiceReader: Reader[Configuration, EmailService] = Reader(conf => EmailService(conf.emailReplyTo))

  def emailUser(username: String, userEmail: String): String = {
    //fetch the status of their last order
    //email them with the EmailService: "Your order has the status: (status)"
    val sendEmailToUser = for {
      conn <- dbReader
      lastOrderId     = conn.getLastOrderId(username)
      lastOrderStatus = conn.getOrderStatus(lastOrderId)
      emailContent <- emailServiceReader.map(_.sendEmail(userEmail, s"Your order has the status: $lastOrderStatus"))
    } yield emailContent

    sendEmailToUser.run(config)
  }

  //TODO 2: What programming pattern do Readers remind you of?

  def main(args: Array[String]): Unit = {
    println(getLastOrderStatus("daniel"))
    println(emailUser("daniel", "daniel@rockthejvm.com"))
  }
}
