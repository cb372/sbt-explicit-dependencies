package explicitdeps

import sbt.Keys._
import sbt._
import sbt.internal.inc.Analysis

object ExplicitDepsPlugin extends AutoPlugin {

  object UndeclaredCompileDependenciesException extends FeedbackProvidedException {
    override def toString: String = "Failing the build because undeclared dependencies were found"
  }

  object UnusedCompileDependenciesException extends FeedbackProvidedException {
    override def toString: String = "Failing the build because unused dependencies were found"
  }

  object autoImport {
    val undeclaredCompileDependencies = taskKey[Set[Dependency]]("find all libraries that this project's code directly depends on for compilation, but which are not declared in libraryDependencies")
    val undeclaredCompileDependenciesTest = taskKey[Unit]("fail the build if there are any libraries that have not been explicitly declared as compile-time dependencies")

    val unusedCompileDependencies = taskKey[Set[Dependency]]("find all libraries declared in libraryDependencies that this project's code does not actually depend on for compilation")
    val unusedCompileDependenciesTest = taskKey[Unit]("fail the build if there are any libraries declared in libraryDependencies that this project's code does not actually depend on for compilation")
  }
  import autoImport._

  override def trigger = allRequirements
  override def requires = empty
  override lazy val projectSettings = Seq(
    undeclaredCompileDependencies := undeclaredCompileDependenciesTask.value,
    undeclaredCompileDependenciesTest := undeclaredCompileDependenciesTestTask.value,

    unusedCompileDependencies := unusedCompileDependenciesTask.value,
    unusedCompileDependenciesTest := unusedCompileDependenciesTestTask.value
  )

  lazy val undeclaredCompileDependenciesTask = Def.task {
    val projectName = name.value
    val compileAnalysis = compile.in(Compile).value.asInstanceOf[Analysis]
    val libraryDeps = libraryDependencies.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val log = streams.value.log

    val compileDependencies = getCompileDependencies(compileAnalysis, scalaBinaryVer, log)
    val declaredCompileDependencies = getDeclaredCompileDependencies(libraryDeps, scalaBinaryVer, log)

    val undeclaredCompileDependencies = compileDependencies diff declaredCompileDependencies
    if (undeclaredCompileDependencies.nonEmpty) {
      val sorted = undeclaredCompileDependencies.toList.sortBy(dep => s"${dep.organization} ${dep.name}")
      log.warn(
        s"""$projectName >>> The project depends on the following libraries for compilation but they are not declared in libraryDependencies:
          | - ${sorted.mkString("\n - ")}""".stripMargin)
    } else {
      log.info(s"$projectName >>> The project explicitly declares all the libraries that it directly depends on for compilation. Good job!")
    }

    undeclaredCompileDependencies
  }

  lazy val undeclaredCompileDependenciesTestTask = Def.task {
    val undeclaredCompileDeps = undeclaredCompileDependencies.value
    if (undeclaredCompileDeps.nonEmpty)
      throw UndeclaredCompileDependenciesException
  }

  lazy val unusedCompileDependenciesTask = Def.task {
    val projectName = name.value
    val compileAnalysis = compile.in(Compile).value.asInstanceOf[Analysis]
    val libraryDeps = libraryDependencies.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val log = streams.value.log

    val compileDependencies = getCompileDependencies(compileAnalysis, scalaBinaryVer, log)
    val declaredCompileDependencies = getDeclaredCompileDependencies(libraryDeps, scalaBinaryVer, log)

    val unusedCompileDependencies = declaredCompileDependencies diff compileDependencies
    if (unusedCompileDependencies.nonEmpty) {
      val sorted = unusedCompileDependencies.toList.sortBy(dep => s"${dep.organization} ${dep.name}")
      log.warn(
        s"""$projectName >>> The following libraries are declared in libraryDependencies but are not needed for compilation:
           | - ${sorted.mkString("\n - ")}""".stripMargin)
    } else {
      log.info(s"$projectName >>> The project has no unused dependencies declared in libraryDependencies. Good job!")
    }

    unusedCompileDependencies
  }

  lazy val unusedCompileDependenciesTestTask = Def.task {
    val unusedCompileDeps = unusedCompileDependencies.value
    if (unusedCompileDeps.nonEmpty)
      throw UnusedCompileDependenciesException
  }

  private def getCompileDependencies(compileAnalysis: Analysis, scalaBinaryVersion: String, log: Logger): Set[Dependency] = {
    val compileDependencyJarFiles =
      compileAnalysis.relations.allLibraryDeps
        .filter(_.getName.endsWith(".jar"))
        .filterNot(_.getName == "rt.jar") // Java runtime
        .filterNot(_.getName == "scala-library.jar")

    val compileDependencies = compileDependencyJarFiles
      .flatMap(BoringStuff.jarFileToDependency(scalaBinaryVersion, log))
    log.debug(s"Compile depends on:\n${compileDependencies.mkString("\n")}")

    compileDependencies.toSet
  }

  private def getDeclaredCompileDependencies(libraryDependencies: Seq[ModuleID], scalaBinaryVersion: String, log: Logger): Set[Dependency] = {
    val compileConfigLibraryDependencies = libraryDependencies
      .filter(_.configurations.fold[Boolean](true)(_.contains("compile")))
      .filterNot(_.name == "scala-library")

    val declaredCompileDependencies = compileConfigLibraryDependencies
      .map(moduleId => Dependency(moduleId.organization, moduleId.name, moduleId.revision, moduleId.crossVersion == Binary()))
    log.debug(s"Declared dependencies:\n${declaredCompileDependencies.mkString("\n")}")

    declaredCompileDependencies.toSet
  }

}
