import ReleaseTransformations._

enablePlugins(SbtPlugin)
crossSbtVersions := Seq("0.13.18", "1.2.8")

scalaVersion := "2.12.8"
organization := "com.github.cb372"
description := "An sbt plugin to check that your project does not directly depend on any transitive dependencies for compilation"
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

// This goes in build.sbt, not in project/plugins.sbt, because we are a plugin depending on another plugin.
// "It's funky, but it's right." -- @dwijnand
addSbtPlugin("com.dwijnand" % "sbt-compat" % "1.2.6")

releaseCrossBuild := false
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ scripted"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publish"),
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
  "-Dscala.version=2.12.9",
  s"-Dsbt.boot.directory=${file(sys.props("user.home")) / ".sbt" / "boot"}"
)
scriptedBufferLog := false
