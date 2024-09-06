enablePlugins(SbtPlugin)

val latestSbt_1_3_x_version = "1.3.13"
val latestSbt_1_4_x_version = "1.4.0"
val latestSbt_1_8_x_version = "1.8.3"

crossSbtVersions := Seq(
  latestSbt_1_3_x_version,
  latestSbt_1_4_x_version,
  latestSbt_1_8_x_version
)

scalaVersion := "2.12.20"
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
  "-Dscala.version=2.12.11",
  s"-Dsbt.boot.directory=${file(sys.props("user.home")) / ".sbt" / "boot"}"
)
scriptedBufferLog := false
