scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0"
)
addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.13.4").cross(CrossVersion.full))
