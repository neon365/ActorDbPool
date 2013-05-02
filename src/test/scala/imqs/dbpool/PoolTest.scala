package imqs.dbpool

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import akka.actor.ActorSystem
import scala.concurrent.{Future, Await}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.sql.ResultSet
import scala.collection.parallel.mutable

@RunWith(classOf[JUnitRunner])
class PoolTest extends FunSpec {

  val db = DbDetails("jdbc:h2:mem:test","","")
  Class.forName("org.h2.Driver").newInstance()

  describe("Pool creation") {
    it("should create pool of N actors that are connected to the DB on construction") {
      val size = 5
      val test = DbPool(ActorSystem("DbPool"),size,db)
      implicit val timeout = Timeout(5 seconds)
      val futures = for (i ← 1 to 10) yield ask(test, Status).mapTo[String]

      var s = collection.mutable.Set[String]();
      for (f <- futures) {
        val res = Await.result(f,1 second)
        s.add(res)
      }
      // Set should contain "size" unique entries
      assert(s.size == size)
    }
  }

  describe("DB Interaction") {
    it("should create a table, populate it and check that the DB was correctly populated") {
      val test = DbPool(ActorSystem("DbPool"),2,db)
      implicit val timeout = Timeout(5 seconds)

      // Clean out the table if it exists
      val drop = test ? Execute("drop table if exists test")
      Await.result(drop,timeout.duration).asInstanceOf[Some[ResultSet]]
      val future = test ? Execute("create table test (id int, name varchar)")
      val res = Await.result(future,timeout.duration).asInstanceOf[Some[ResultSet]]
      println(res)

    }
  }

}
