name := """play-scala-seed"""
organization := "f"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.tpolecat" %% "skunk-core" % "0.6.3",
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.5",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "org.playframework" %% "play-slick" % "6.1.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.5.0",
  "com.typesafe.play" %% "play-json" % "2.10.6",
  "org.postgresql" % "postgresql" % "42.7.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.2",
  "org.mindrot" % "jbcrypt" % "0.4"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "f.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "f.binders._"
