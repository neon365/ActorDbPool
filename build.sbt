name := "DatabasePool"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= List(
  "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP13",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.1.2",
  "com.h2database" % "h2" % "1.3.171",
  "junit" % "junit" % "4.11"
)
