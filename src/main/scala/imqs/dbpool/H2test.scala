package imqs.dbpool

import java.sql.DriverManager

/**
 * User: weber
 * Date: 2013/04/30
 * Time: 1:38 PM
 */
object H2test extends App {

  val driver = "org.h2.Driver"
  val url = "jdbc:h2:///Users/weber/test"
  val user = ""
  val pwd = ""
  Class.forName(driver)
  val connection = DriverManager.getConnection(url,user,pwd)


}
