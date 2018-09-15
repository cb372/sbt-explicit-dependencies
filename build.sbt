enablePlugins(SbtPlugin)

version := "0.2.1"
scalaVersion := "2.12.6"
organization := "com.github.cb372"
description := "An sbt plugin to check that your project does not directly depend on any transitive dependencies for compilation"
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization in bintray := None


