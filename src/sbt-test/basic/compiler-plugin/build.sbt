scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0"
)
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
