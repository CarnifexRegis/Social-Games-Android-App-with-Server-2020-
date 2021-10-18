name := """AndroidTutorialServer2"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies += guice

libraryDependencies += javaJdbc
libraryDependencies += cache
libraryDependencies += javaWs

libraryDependencies ++= Seq("uk.co.panaxiom" %% "play-jongo" % "2.0.0-jongo1.3")

libraryDependencies += "com.google.android.gcm" % "gcm-server" % "1.0.2"

libraryDependencies += "com.restfb" % "restfb" % "1.6.14"

libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.27.2.1"

PlayKeys.devSettings := Seq("play.akka.dev-mode.akka.http.parsing.max-uri-length" -> "10000000")

resolvers += "GCM Server Repository" at
"https://raw.github.com/slorber/gcm-server-repository/master/releases/"