scalaVersion := "2.12.6"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-free" % "1.4.0"
)
undeclaredCompileDependenciesFilter -= moduleFilter(organization = "org.typelevel", name = "cats-core")
