name := "url2har"

version := "0.2"

description := "URL list to HAR files."

organization := "dk.industria.url2har"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlint", "-language:postfixOps")

scalaVersion := "2.12.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16" % "compile"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.16" % "compile"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.1" % "compile"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"

libraryDependencies += "io.github.bonigarcia" % "webdrivermanager" % "1.5.1"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "3.0.1" % "compile"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5" % "compile"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"



