name := "luastages-scala"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.+",
  "org.luaj" % "luaj-jse" % "3.+",
  "junit" % "junit" % "4.+",
  "junit" % "junit" % "4.+" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test"
)

