lazy val root = project.aggregate(
  scala2,
  scala3
)

lazy val scala2 = project.settings(
  scalaVersion := sys.props("scala.version")
)

lazy val scala3 = project
  .settings(
    scalaVersion := "3.8.3"
  )
