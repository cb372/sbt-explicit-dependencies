scalaVersion in ThisBuild := sys.props("scala.version")

val commonDeps = Seq(
  "org.typelevel" %% "cats-core" % "1.4.0"
)

val coreDeps = commonDeps ++ Seq("com.chuusai" %% "shapeless" % "2.3.3")
val core = project.in(file("core"))
  .settings(libraryDependencies ++= coreDeps)

val toolsDeps = commonDeps ++ Seq("org.scalameta" %% "trees" % "4.3.0")
val tools = project.in(file("tools"))
  .settings(libraryDependencies ++= toolsDeps)

val root = project.in(file("."))
  .aggregate(core, tools)
