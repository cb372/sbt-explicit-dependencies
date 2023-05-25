libraryDependencies ++= Seq(
  "com.raquo" %%% "laminar" % "0.11.0",
  "com.raquo" %%% "airstream" % "0.11.0",
  "com.raquo" %%% "domtypes" % "0.10.1",
  "org.scala-js" %%% "scalajs-dom" % "1.1.0"
)

scalaJSUseMainModuleInitializer := true

enablePlugins(ScalaJSPlugin)
