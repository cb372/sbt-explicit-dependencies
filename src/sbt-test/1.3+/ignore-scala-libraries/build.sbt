lazy val root = project.aggregate(
  scala2, 
  scala3, 
  scalajs
)

lazy val scala2 = project.settings(
  scalaVersion := sys.props("scala.version")
)

lazy val scalajs = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaVersion := sys.props("scala.version")
  )

lazy val scala3 = project
  .settings(
    scalaVersion := "3.0.0-M1",
  )
