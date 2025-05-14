package explicitdeps

import sbt.Keys._
import sbt.{ScalaVersion => _, _}

object ExplicitDepsPlugin extends AutoPlugin {

  trait Implicits {
    implicit val moduleFilterRemoveValue: Remove.Value[ModuleFilter, ModuleFilter] =
      new Remove.Value[ModuleFilter, ModuleFilter] {
        override def removeValue(a: ModuleFilter, b: ModuleFilter): ModuleFilter = a - b
      }
  }

  object autoImport extends Implicits {
    val undeclaredCompileDependencies = taskKey[Set[Dependency]]("find all libraries that this project's code directly depends on for compilation, but which are not declared in libraryDependencies")
    val undeclaredCompileDependenciesTest = taskKey[Unit]("fail the build if there are any libraries that have not been explicitly declared as compile-time dependencies")
    val undeclaredCompileDependenciesFilter = settingKey[ModuleFilter]("Filter to specify the undeclared dependencies that you care about")

    val unusedCompileDependencies = taskKey[Set[Dependency]]("find all libraries declared in libraryDependencies that this project's code does not actually depend on for compilation")
    val unusedCompileDependenciesAggregate = taskKey[Set[Dependency]]("find all libraries declared in libraryDependencies that this project's code does not actually depend on for compilation, aggregated for all subprojects")
    val unusedCompileDependenciesTest = taskKey[Unit]("fail the build if there are any libraries declared in libraryDependencies that this project's code does not actually depend on for compilation")
    val unusedCompileDependenciesAggregateTest = taskKey[Unit]("fail the build if there are any libraries declared in libraryDependencies that this project's code does not actually depend on for compilation, aggregated for all subprojects")
    val unusedCompileDependenciesFilter = settingKey[ModuleFilter]("Filter to specify the undeclared dependencies that you care about")
  }
  import autoImport._

  override def trigger = allRequirements
  override def requires = sbt.plugins.IvyPlugin
  override lazy val projectSettings = Seq(
    undeclaredCompileDependencies := undeclaredCompileDependenciesTask.value,
    undeclaredCompileDependenciesTest := undeclaredCompileDependenciesTestTask.value,
    undeclaredCompileDependenciesFilter := defaultModuleFilter,

    unusedCompileDependencies := unusedCompileDependenciesTask.value,
    unusedCompileDependenciesAggregate := unusedCompileDependenciesAggregateTask.value,
    unusedCompileDependenciesTest := unusedCompileDependenciesTestTask.value,
    unusedCompileDependenciesAggregateTest := unusedCompileDependenciesAggregateTestTask.value,
    unusedCompileDependenciesFilter := defaultModuleFilter,

    unusedCompileDependenciesAggregate / aggregate := false,
    unusedCompileDependenciesAggregateTest / aggregate := false
  )

  // Evaluate "csrCacheDirectory" setting which is present only in sbt 1.3.0 or newer
  private lazy val csrCacheDirectoryValueTask = Def.task {
    val extracted: Extracted = Project.extract(state.value)
    val settings = extracted.session.original
    settings.find(_.key.key.label == "csrCacheDirectory") match {
      case Some(csrCacheDirectorySetting) =>
        val csrCacheDirectoryValue = csrCacheDirectorySetting.init.evaluate(extracted.structure.data).toString
        Some(csrCacheDirectoryValue)
      case _ => None
    }
  }

  private lazy val collectLibraryDepsProjectTask = Def.task {
    val log = streams.value.log

    val compileAnalysis = (Compile / compile).value.asInstanceOf[Analysis]
    val testAnalysis = (Test / compile).value.asInstanceOf[Analysis]

    val csrCacheDirectoryOpt = csrCacheDirectory.?.value.map(_.getAbsolutePath)
    val baseDir = baseDirectory.value.getAbsolutePath

    val usedCompileDeps: Set[File] = getAllLibraryDeps(compileAnalysis, log)(csrCacheDirectoryOpt, baseDir)
    val usedTestDeps: Set[File] = getAllLibraryDeps(testAnalysis, log)(csrCacheDirectoryOpt, baseDir)
    val declaredDeps: Seq[ModuleID] = libraryDependencies.value

    log.debug(s"[${name.value}] Found ${(usedCompileDeps ++ usedTestDeps).size} used jars and ${declaredDeps.size} declared modules.")

    (usedCompileDeps, usedTestDeps, declaredDeps)
  }

  private lazy val collectLibraryDepsAllTask = Def.task {
    val log = streams.value.log

    val projectResults: Seq[(Set[File], Set[File], Seq[ModuleID])] = collectLibraryDepsProjectTask.all(
      ScopeFilter(inAnyProject, inAnyConfiguration)
    ).value

    val (allUsedCompileDeps, allUsedTestDeps, allDeclaredDeps) = projectResults.unzip3

    val mergedUsedCompileDeps = allUsedCompileDeps.flatten.toSet
    val mergedUsedTestDeps = allUsedTestDeps.flatten.toSet
    val mergedDeclaredDeps = allDeclaredDeps.flatten.toSet

    log.debug(s"Aggregated ${(mergedUsedCompileDeps ++ mergedUsedTestDeps).size} unique used jars and ${mergedDeclaredDeps.size} unique declared modules across all projects.")
    log.debug("Aggregated Used Compile Dependencies:")
    mergedUsedCompileDeps.toSeq.sorted.foreach(dep => log.debug(s"  - ${dep.getName}"))
    log.debug("Aggregated Used Test Dependencies:")
    mergedUsedTestDeps.toSeq.sorted.foreach(dep => log.debug(s"  - ${dep.getName}"))

    log.debug("Aggregated Declared Dependencies:")
    val namePadding = mergedDeclaredDeps.map(_.name.length).max + 3
    mergedDeclaredDeps
      .toSeq.sorted(Ordering.by[ModuleID, String](_.name))
      .foreach { dep =>
        val paddedName = dep.name.padTo(namePadding, ' ')
        log.debug(s"  - $paddedName${dep.organization} % ${dep.name} % ${dep.revision}")
      }
    (mergedUsedCompileDeps, mergedUsedTestDeps, mergedDeclaredDeps.toSeq)
  }

  lazy val undeclaredCompileDependenciesTask = Def.task {
    val log = streams.value.log
    val projectName = name.value
    val csrCacheDirectoryValueOpt = csrCacheDirectoryValueTask.value
    val baseDirectoryValue = appConfiguration.value.baseDirectory().getCanonicalFile.toPath.toString
    val allLibraryDeps = getAllLibraryDeps((Compile / compile).value.asInstanceOf[Analysis], log)(csrCacheDirectoryValueOpt, baseDirectoryValue)
    val libraryDeps = libraryDependencies.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val scalaFullVer = scalaVersion.value
    val filter = undeclaredCompileDependenciesFilter.value

    Logic.getUndeclaredCompileDependencies(
      projectName,
      allLibraryDeps,
      libraryDeps,
      ScalaVersion(scalaBinaryVer, scalaFullVer),
      filter,
      log
    )
  }

  lazy val undeclaredCompileDependenciesTestTask = Def.task {
    val undeclaredCompileDeps = undeclaredCompileDependencies.value
    if (undeclaredCompileDeps.nonEmpty)
      throw UndeclaredCompileDependenciesException
  }

  lazy val unusedCompileDependenciesTask = Def.task {
    val log = streams.value.log
    val projectName = name.value
    val csrCacheDirectoryValueOpt = csrCacheDirectoryValueTask.value
    val baseDirectoryValue = appConfiguration.value.baseDirectory().getCanonicalFile.toPath.toString
    val allLibraryDeps = getAllLibraryDeps((Compile / compile).value.asInstanceOf[Analysis], log)(csrCacheDirectoryValueOpt, baseDirectoryValue)
    val allLibraryTestDeps = getAllLibraryDeps((Test / compile).value.asInstanceOf[Analysis], log)(csrCacheDirectoryValueOpt, baseDirectoryValue)
    val libraryDeps = libraryDependencies.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val scalaFullVer = scalaVersion.value
    val filter = unusedCompileDependenciesFilter.value

    Logic.getUnusedCompileDependencies(
      projectName,
      allLibraryDeps,
      allLibraryTestDeps,
      libraryDeps,
      ScalaVersion(scalaBinaryVer, scalaFullVer),
      filter,
      log
    )
  }

  lazy val unusedCompileDependenciesTestTask = Def.task {
    val unusedCompileDeps = unusedCompileDependencies.value
    if (unusedCompileDeps.nonEmpty)
      throw UnusedCompileDependenciesException
  }

  lazy val unusedCompileDependenciesAggregateTask = Def.task {
    val log = streams.value.log
    val projectName = name.value
    val (usedCompileDeps, usedTestDeps, declaredDeps) = collectLibraryDepsAllTask.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val scalaFullVer = scalaVersion.value
    val filter = unusedCompileDependenciesFilter.value

    Logic.getUnusedCompileDependencies(
      projectName,
      usedCompileDeps,
      usedTestDeps,
      declaredDeps,
      ScalaVersion(scalaBinaryVer, scalaFullVer),
      filter,
      log
    )
  }

  lazy val unusedCompileDependenciesAggregateTestTask = Def.task {
    val unusedCompileDeps = unusedCompileDependenciesAggregate.value
    if (unusedCompileDeps.nonEmpty)
      throw UnusedCompileDependenciesException
  }
}
