name := "url2har"

version := "0.1"

description := "URL list to HAR files."

organization := "dk.industria.url2har"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimise", "-feature", "-Xlint")

scalaVersion := "2.10.2"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.1.0"

libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.35.0" % "compile"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1" % "compile"

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.3" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

assemblySettings

