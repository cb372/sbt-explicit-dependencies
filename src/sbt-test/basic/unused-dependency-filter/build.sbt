scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.scalaz" %% "scalaz" % "7.2.26"
)
unusedCompileDependenciesFilter -= moduleFilter(organization = "org.scalaz", name = "scalaz")
