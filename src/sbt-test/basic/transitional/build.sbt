import sbt.CrossVersion.for3Use2_13

val commonDeps = Seq(
  "org.typelevel" %% "cats-core" % "2.13.0" cross for3Use2_13,
)

val coreDeps = commonDeps ++ Seq("org.typelevel" %% "shapeless3-typeable" % "3.5.0")
val core = project
  .in(file("core"))
  .settings(libraryDependencies ++= coreDeps, scalaVersion := "3.7.4")

val toolsDeps = commonDeps ++ Seq("org.scalameta" %% "trees" % "4.3.0" cross for3Use2_13)
val tools = project
  .in(file("tools"))
  .settings(libraryDependencies ++= toolsDeps, scalaVersion := "2.13.18")

val root = project
  .in(file("."))
  .aggregate(core, tools)
