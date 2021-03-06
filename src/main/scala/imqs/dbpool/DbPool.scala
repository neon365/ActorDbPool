package imqs.dbpool

import java.sql.{DriverManager,Connection}
import akka.actor._
import akka.routing.RoundRobinRouter

/**
 * Contains the credentials and access details for the DB
 * @param db  A URL pointing to the database (e.g.jdbc:postgresql://foo.com:5432/dbname)
 * @param user User name
 * @param pwd Password
 */
case class DbDetails( db: String, user: String, pwd: String)

case class Execute(statement: String)

case class Update(statement: String)

case class Query(statement: String)

// Returns the status of the actor, including its path
case class Status()

/**
 * Actor class that will be a member of the connection pool
 * @param detail The database details to which we are expected to connect
 */
private class PoolActor (detail: DbDetails ) extends Actor with ActorLogging {

  lazy val connection: Connection = DriverManager.getConnection(detail.db,detail.user,detail.pwd)

  override def preStart() {
    log.info("DB Pool actor started: " + this.toString)
  }

  override def postStop() {
    connection.close()
    log.info("DB Pool terminated: " + this.toString)
  }

  def receive = {
    case Execute(sql: String) =>
      val statement = connection.createStatement()
      sender ! statement.execute(sql) // Boolean
      statement.close()
      log.info(s"Execute('$sql') completed" )
    case Query(sql: String) =>
      val statement = connection.createStatement()
      sender ! statement.executeQuery(sql) // ResultSet
      log.info(s"Query('$sql') completed" )
    case Update(sql:String) =>
      val statement = connection.createStatement()
      sender ! statement.executeUpdate(sql) // Int
      statement.close()
      log.info(s"Update('$sql') completed" )
    case Status => sender ! self.path.name
    case x => log.error(s"Invalid message type in PoolActor: ${x.getClass}")
  }
}

object PoolActor {
  /**
   * Factory to generate the Props required when creating the actor pool
   * @param detail Database details
   * @return A Props configuration class for a PoolActor
   */
  def apply(detail: DbDetails): Props = Props(new PoolActor(detail))
}


object DbPool {

  /**
   * Create a router (actor) to access the database pool. It will create the required number of connections
   * and dispatch any requests (formulated as futures) to the next member of the pool to execute the query.
   * Sending the actor an @see Update results in an integer return. For a @see Query, a JDBC ResultSet is
   * returned. In the case of an @see Execute message, a Boolean is returned. These messages correspond to
   * calls to execute(), executeQuery() and executeUpdate() calls on a Statement object. In the case of a
   * ResultSet being returned, it is the responsibility of the calling actor to close the statement.
   * The results are returned as a ResultSet as is standard for JDBC.
   * TODO: Make this a future rather than an actor style call
   * @param system The actor system
   * @param size The size of the connection pool
   * @param db A DbDetail containing all the credentials for the database
   * @return An ActorRef referencing the underlying router
   */
  def apply(system: ActorSystem, size: Int, db: DbDetails): ActorRef = {
    system.actorOf(PoolActor(db).withRouter(RoundRobinRouter(nrOfInstances = size)))
  }
}


