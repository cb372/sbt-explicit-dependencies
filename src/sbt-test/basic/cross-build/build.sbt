scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.11.12", scalaVersion.value)
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-free" % "1.4.0",
  "org.scalaz" %% "scalaz" % "7.2.26"
)
