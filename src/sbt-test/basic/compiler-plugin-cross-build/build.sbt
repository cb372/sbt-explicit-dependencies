scalaVersion := sys.props("scala.version")
crossScalaVersions := Seq("2.13.18", scalaVersion.value)
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.9.0"
)
addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.13.4").cross(CrossVersion.full))
