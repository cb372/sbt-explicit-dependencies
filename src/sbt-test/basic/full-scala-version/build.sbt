scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "ammonite-interp-api" % "2.2.0-4-4bd225e" cross(CrossVersion.full)
)
