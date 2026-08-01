name := "scala-play-quarterly-tax-service"

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
    ),
    Test / fork := true,
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )
