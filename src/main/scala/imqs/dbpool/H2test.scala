package imqs.dbpool

import java.sql.DriverManager

/**
 * User: weber
 * Date: 2013/04/30
 * Time: 1:38 PM
 */
object H2test extends App {

  val driver = "org.h2.Driver"
  val url = "jdbc:h2:mem:test_fu"
  val user = ""
  val pwd = ""
  Class.forName(driver)
  val connection = DriverManager.getConnection(url,user,pwd)
  val res = connection.createStatement().execute("create table toot (id int, name varchar)")
  println(res)
}
