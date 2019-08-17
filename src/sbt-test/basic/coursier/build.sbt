scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-free" % "1.4.0",
  "org.scalaz" %% "scalaz" % "7.2.26"
)
