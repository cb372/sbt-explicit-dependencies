scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % "0.18.16",
  "org.http4s" %% "http4s-circe" % "0.18.16",
  "org.tpolecat" %% "doobie-postgres" % "0.5.3",
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

val undeclaredDeps = taskKey[String]("find undeclared dependencies")

undeclaredDeps := {
  val configsWeCareAbout = Set("compile")
  val updateReport = update.value
  val libraryDeps = libraryDependencies.value
  val scalaVersionSuffix = s"_${scalaBinaryVersion.value}"

  val allCompileDeps =
    for {
      configReport <- updateReport.configurations.filter(configRef => configsWeCareAbout.contains(configRef.configuration.name))
      moduleReport <- configReport.modules if !moduleReport.evicted
    } yield moduleReport.module

  val declaredDeps = libraryDeps.map(dep => (dep.organization, dep.name)).toSet

  val undeclaredCompileDeps = allCompileDeps.filterNot { module =>
    val normalisedName = module.name.replace(scalaVersionSuffix, "")
    declaredDeps.contains((module.organization, normalisedName))
  }

  undeclaredCompileDeps
    .sortBy(module => (module.organization, module.name))
    .map { module =>
      if (module.name.endsWith(scalaVersionSuffix))
        s""" "${module.organization}" %% "${module.name.replace(scalaVersionSuffix, "")}" % "${module.revision}", """
      else
        s""" "${module.organization}"  % "${module.name}" % "${module.revision}", """
    }
    .mkString("\n")
}
