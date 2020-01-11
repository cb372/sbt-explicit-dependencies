scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0" % Optional
)
