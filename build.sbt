
val ScalaVersion = "2.13.1"
val ScalacOptions = Seq(
//  "-encoding", "utf8",
//  "-deprecation",
//  "-feature",
//  "-Xfatal-warnings",
//  "-unchecked",
//  "-Xlog-reflective-calls",
  "-Xlint",
//  "-language:implicitConversions",
//  "-language:higherKinds",
//  "-language:existentials",
//  "-language:postfixOps"
)

val AkkaVersion = "2.6.3"
val AkkaHttpVersion = "10.1.11"
val AkkaPersistenceJdbcVersion = "3.5.2"
val AkkaStreamVersion = AkkaVersion

// TODO: Add https://github.com/fthomas/scala-steward
// TODO: Add https://scalacenter.github.io/scalafix/
lazy val akkaStarter = project
  .in(file("."))
  .settings(
    organization := "io.josh.akkastarter",
    version := "0.0.1",
    scalaVersion := ScalaVersion,
    scalacOptions in Compile ++= ScalacOptions,
    javacOptions in Compile ++= Seq(
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
      "com.github.dnvriend" %% "akka-persistence-jdbc" % AkkaPersistenceJdbcVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "mysql" % "mysql-connector-java" % "8.0.19",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
    ),
    fork in run := false,
    Global / cancelable := false, // ctrl-c
    mainClass in (Compile, run) := Some("io.josh.akkastarter.Main"),
    // disable parellel tests
    parallelExecution in Test := false,
    // show full stack traces and test case durations
    testOptions in Test += Tests.Argument("-oDF"),
    logBuffered in Test := false,
  )
