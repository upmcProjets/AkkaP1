lazy val root = (project in file(".")).
  settings(
    name := "exemple2",
    version := "1.0",
    scalaVersion := "2.11.4"
  )
  

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.0"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.0"
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.60-R9"


