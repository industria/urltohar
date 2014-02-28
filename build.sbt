name := "url2har"

version := "0.2"

description := "URL list to HAR files."

organization := "dk.industria.url2har"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimise", "-feature", "-Xlint", "-language:postfixOps")

scalaVersion := "2.10.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3" % "compile"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2.3" % "compile"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.1" % "compile"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.1.0"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.40.0" % "compile"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5" % "compile"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

assemblySettings

