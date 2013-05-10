package imqs.dbpool

import akka.actor.ActorSystem
import akka.pattern.ask
import java.sql.ResultSet
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * User: weber
 * Date: 2013/04/30
 * Time: 1:38 PM
 */
object PoolExample extends App {

  Class.forName("org.h2.Driver").newInstance()
  val db = DbDetails("jdbc:h2:mem:test_fu","","")
  implicit val timeout = Timeout(5 seconds)
  val system = ActorSystem("DbPool")
  val pool = DbPool(system,2,db)

  // Returns a list of all  the functions available in H2
  val future = pool ? Query("help")

  future onComplete {
    case Success(res: ResultSet) =>
      while (res.next()) print(res.getString(3) + ", ")
      println("\nRequest completed!")
    case Failure(error) =>
      println("Query failed: " + error)
  }

  println("Pool request dispatched")
  Thread.sleep(1000)
  system.shutdown()
}
