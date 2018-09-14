scalaVersion := "2.12.6"
name := "example"

libraryDependencies ++= Seq(
  // this brings in guava as a transitive dependency, which is used directly for compilation
  "org.http4s" %% "http4s-blaze-server" % "0.18.16",

  // this brings in cats-effect and cats-core as transitive dependencies, which are used directly for compilation
  "org.http4s" %% "http4s-circe" % "0.18.16",

  // this brings in cats-effect and cats-core as transitive dependencies, which are used directly for compilation
  "org.tpolecat" %% "doobie-postgres" % "0.5.3",

  // this is just an example of a dependency that is irrelevant to the sbt plugin
  "org.postgresql" % "postgresql" % "42.2.5",

  // this brings in guava as a transitive dependency, which is used directly for compilation
  "com.github.cb372" %% "scalacache-guava" % "0.24.3",

  // cats-core is declared as a test dependency,
  // but it is also a transitive dependency in the Compile config
  // and it is depended on directly for compilation
  "org.typelevel" %% "cats-core" % "1.2.0" % Test
)
