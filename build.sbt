import ReleaseTransformations._

enablePlugins(SbtPlugin)

scalaVersion := "2.12.6"
organization := "com.github.cb372"
description := "An sbt plugin to check that your project does not directly depend on any transitive dependencies for compilation"
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
publishMavenStyle := false
bintrayRepository := "sbt-plugins"
bintrayOrganization in bintray := None

releaseCrossBuild := false
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  //releaseStepCommandAndRemaining("test"), TODO write some damn tests
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publish"),
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
