name := "calendar-holidays"

version in Global := "1.0"

scalaVersion in Global := "2.11.8"

val `com.typesafe.scala-logging_scala-logging` = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
//val `pl.warsawscala.calendar` = "pl.warsawscala.calendar" %% "calendar-holidays" % "1.0"
val `com.typesafe.play` = "com.typesafe.play" %% "play-ws" % "2.4.8"

lazy val restapi = Project("restapi", file("restapi"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin).settings(
  libraryDependencies ++= Seq(
    `com.typesafe.scala-logging_scala-logging`,
    ws,
    `com.typesafe.play`
  )
)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-mock" % "3.8" % "test",
  `com.typesafe.play`
)
