package imqs.dbpool

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import akka.actor.ActorSystem
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.sql.ResultSet
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class PoolTest extends FunSpec {

  val db = DbDetails("jdbc:h2:mem:pooltest", "", "")
  Class.forName("org.h2.Driver").newInstance()
  implicit val timeout = Timeout(5 seconds)

  describe("Pool creation") {
    it("should create pool of N actors that are connected to the DB on construction") {
      val size = 5
      val actorSystem = ActorSystem("DbPool")
      val test = DbPool(actorSystem, size, db)
      val futures = for (i ← 1 to 2 * size) yield test ? Status

      val s = collection.mutable.Set[String]()
      for (f <- futures) {
        Await.result(f, 1 second) match {
          case res: String => s.add(res)
          case _ => fail("Actor can only return a string if it is sent a status message")
        }
      }
      // Set should contain "size" unique entries
      assert(s.size == size)
    }
  }

  describe("Database connections") {
    it("should create pool of N database connections") {
      val size = 5
      val actorSystem = ActorSystem("DbPool")
      val test = DbPool(actorSystem, size, db)

      // Check that the database is actually there
      val cf = for (i ← 1 to 2 * size) yield test ? Query("help")
      for (f <- cf) {
        Await.result(f, 5 seconds) match {
          case res: ResultSet => assert(res.next())
          case _ => fail("Actor must return a result set for the query")
        }
      }
    }
  }

  describe("DB Interaction") {
    it("should create a table, populate it and check that the DB was correctly populated") {
      val test = DbPool(ActorSystem("DbPool"), 2, db)
      val f0 = test ? Execute("create table test (id int, name varchar)")
      Await.result(f0, 1 seconds) match {
        case r: Boolean => assert(!r)
        case _ => fail("Only true if there was a ResultSet returned")
      }

      val f1 = test ? Update("insert into test values(1,'foo')")
      Await.result(f1, 1 seconds) match {
        case count: Int => assert(count == 1)
        case _ => fail("Updates can only return a boolean")
      }

      val f2 = test ? Query("select id,name from test")
      Await.result(f2, 1 seconds) match {
        case r: ResultSet =>
          assert(r.next())
          assert(r.getInt(1).equals(1))
          assert(r.getString(2).equals("foo"))
          r.close() // Does this close the statement?
        case _ => fail("Query must return a ResultSet")
      }
    }
  }
}
