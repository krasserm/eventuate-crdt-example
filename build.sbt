organization := "com.rbmhtechnology"

name := "eventuate-test"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "Eventuate Releases" at "https://dl.bintray.com/rbmhtechnology/maven"

resolvers += "OJO Snapshots" at "https://oss.jfrog.org/oss-snapshot-local"

val eventuateVersion = "0.8-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.rbmhtechnology" %% "eventuate-core" % eventuateVersion,
  "com.rbmhtechnology" %% "eventuate-crdt" % eventuateVersion,
  "com.rbmhtechnology" %% "eventuate-log-leveldb" % eventuateVersion)
