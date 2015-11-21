val commonSettings = Seq(
  organization := "com.github.tkawachi",
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/tkawachi/bitcoin-exercise/"),
    "scm:git:github.com:tkawachi/bitcoin-exercise.git"
  )),

  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint"
  ),

  doctestTestFramework := DoctestTestFramework.ScalaTest
) ++ scalariformSettings ++ doctestSettings

lazy val root = project.in(file("."))
  .settings(commonSettings :_*)
  .settings(
    name := "bitcoin-exercise",
    description := "Bitcoin exercise",
    resolvers += "yzernik repo" at "http://dl.bintray.com/yzernik/maven/",
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "io.github.yzernik" %% "bitcoin-scodec" % "0.2.7"
    )
  )
