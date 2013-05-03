package imqs.dbpool

import akka.actor.ActorSystem
import akka.pattern.ask
import java.sql.ResultSet
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
/**
 * User: weber
 * Date: 2013/04/30
 * Time: 1:38 PM
 */
object H2test extends App {

  Class.forName("org.h2.Driver").newInstance()
  val db = DbDetails("jdbc:h2:mem:test_fu","","")
  implicit val timeout = Timeout(5 seconds)
  val pool = DbPool(ActorSystem("DbPool"),2,db)

  // Returns a list of all  the functions available in H2
  val future = pool ? Query("help")

  future onSuccess {
    case res: ResultSet =>
      while(res.next) {
        print(res.getString(3) + ", ")
      }
      println()
  }
}
