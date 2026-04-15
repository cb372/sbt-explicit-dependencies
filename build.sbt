enablePlugins(SbtPlugin)

val scala212 = "2.12.21"
val scala3 = "3.8.3"

crossScalaVersions := Seq(scala212, scala3)
scalaVersion := scala212

(pluginCrossBuild / sbtVersion) := {
  if (scalaVersion.value.startsWith("2.12.")) "1.12.9"
  else "2.0.0-RC12"
}

name := "sbt-explicit-dependencies"
organization := "com.github.cb372"
description := "An sbt plugin to check that your project does not directly depend on any transitive dependencies for compilation"
homepage := Some(url("https://github.com/cb372/sbt-explicit-dependencies"))
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
developers := List(
  Developer(
    "cb372",
    "Chris Birchall",
    "chris.birchall@gmail.com",
    url("https://github.com/cb372")
  )
)

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-Dplugin.version=" + version.value,
  "-Dscala.version=2.12.21",
  s"-Dsbt.boot.directory=${file(sys.props("user.home")) / ".sbt" / "boot"}"
)
scriptedBufferLog := false
