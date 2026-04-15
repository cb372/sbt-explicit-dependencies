scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "ammonite-interp-api" % "3.0.9" cross(CrossVersion.full)
)
