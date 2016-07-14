organization := "com.potrwerkz"

name := "luastages-scala"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val junitVersion = "4.+"
  val akkaVersion = "2.+"
  Seq(
    "org.apache.commons" % "commons-math3" % "3.+",
    "junit" % "junit" % junitVersion,
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "org.luaj" % "luaj-jse" % "3.+"
  )
}
