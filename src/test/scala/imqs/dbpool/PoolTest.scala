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

@RunWith(classOf[JUnitRunner])
class PoolTest extends FunSpec {

  val db = DbDetails("jdbc:h2:mem:pooltest","","")
  Class.forName("org.h2.Driver").newInstance()
  implicit val timeout = Timeout(5 seconds)

  describe("Pool creation") {
    it("should create pool of N actors that are connected to the DB on construction") {
      val size = 5
      val actorSystem = ActorSystem("DbPool")
      val test = DbPool(actorSystem,size,db)
      val futures = for (i ← 1 to 2*size) yield (test ? Status)

      val s = collection.mutable.Set[String]()
      for (f <- futures) {
        val res = Await.result(f,1 second).asInstanceOf[String]
        s.add(res)
      }
      // Set should contain "size" unique entries
      assert(s.size == size)

      val cf = for (i ← 1 to 2*size) yield test ? Query("help")
      for (f <- cf ) {}
        val res = Await.result(test ? Query("help"),5 seconds).asInstanceOf[ResultSet]
        assert(res.next())
    }
  }

  describe("DB Interaction") {
    it("should create a table, populate it and check that the DB was correctly populated") {
      val test = DbPool(ActorSystem("DbPool"),2,db)
      val f0 = test ? Execute("create table test (id int, name varchar)")
      val r0 = Await.result(f0,1 seconds).asInstanceOf[Boolean]
      assert(!r0) // Only true if there was a ResultSet returned
      val f1 = test ? Update("insert into test values(1,'foo')")
      val count = Await.result(f1,1 seconds).asInstanceOf[Int]
      assert(count == 1)
      val f2 = test ? Query("select id,name from test")
      val r1 = Await.result(f2,1 seconds).asInstanceOf[ResultSet]
      assert(r1.next())
      assert(r1.getInt(1).equals(1))
      assert(r1.getString(2).equals("foo"))
      r1.close() // Does this close the statement?
    }
  }

}
