import ReleaseTransformations._

enablePlugins(SbtPlugin)

val latestSbt_1_2_x_version = "1.2.8"
val latestSbt_1_3_x_version = "1.3.13"
val latestSbt_1_4_x_version = "1.4.0"

crossSbtVersions := Seq(
  latestSbt_1_2_x_version,
  latestSbt_1_3_x_version,
  latestSbt_1_4_x_version
)

scalaVersion := "2.12.11"
organization := "com.github.cb372"
description := "An sbt plugin to check that your project does not directly depend on any transitive dependencies for compilation"
homepage := Some(url("https://github.com/cb372/sbt-explicit-dependencies"))
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

releaseCrossBuild := false
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining(s"^^$latestSbt_1_3_x_version publish"), // Note: if we publish with 1.4.0, the plugin will only work with 1.4.x
  releaseStepTask(updateVersionInExampleProject),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val updateVersionInExampleProject = taskKey[Unit]("update the version of the plugin used in the example project")
updateVersionInExampleProject := {
  val pluginsFile = baseDirectory.value / "example/project/plugins.sbt"
  val content =
    s"""addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "${version.value}")"""
  sbt.io.IO.write(pluginsFile, content)

  val gitCommand = List(
    "git",
    "commit",
    "-m", "Update plugin version in example project",
    "example/project/plugins.sbt"
  )
  scala.sys.process.Process(gitCommand).!
}

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-Dplugin.version=" + version.value,
  "-Dscala.version=2.12.11",
  s"-Dsbt.boot.directory=${file(sys.props("user.home")) / ".sbt" / "boot"}"
)
scriptedBufferLog := false
