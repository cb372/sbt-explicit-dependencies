scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.11.12", scalaVersion.value)
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0"
)
addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.8" cross CrossVersion.binary)
