package explicitdeps

import java.io.File

import sbt.librarymanagement.ModuleID
import sbt.util.Logger

object Logic {

  def getUndeclaredCompileDependencies(
    projectName: String,
    allLibraryDeps: Set[File],
    libraryDeps: Seq[ModuleID],
    scalaVersion: ScalaVersion,
    moduleFilter: ModuleFilter,
    log: Logger
  ): Set[Dependency] = {
    val compileDependencies = getCompileDependencies(allLibraryDeps, scalaVersion, log)
    val declaredCompileDependencies = getDeclaredCompileDependencies(libraryDeps, scalaVersion, log)

    val undeclaredCompileDependencies =
      (compileDependencies diff declaredCompileDependencies)
        .filter(dep => moduleFilter.apply(toModuleID(dep)))

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

  def getUnusedCompileDependencies(
    projectName: String,
    allLibraryDeps: Set[File],
    libraryDeps: Seq[ModuleID],
    scalaVersion: ScalaVersion,
    moduleFilter: ModuleFilter,
    log: Logger
  ): Set[Dependency] = {
    val compileDependencies = getCompileDependencies(allLibraryDeps, scalaVersion, log)
    val declaredCompileDependencies = getDeclaredCompileDependencies(libraryDeps, scalaVersion, log)

    val unusedCompileDependencies =
      (declaredCompileDependencies diff compileDependencies)
        .filter(dep => moduleFilter.apply(toModuleID(dep)))

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

  private def getCompileDependencies(allLibraryDeps: Set[File], scalaVersion: ScalaVersion, log: Logger): Set[Dependency] = {
    val compileDependencyJarFiles =
      allLibraryDeps
        .filter(_.getName.endsWith(".jar"))
        .filterNot(_.getName == "rt.jar") // Java runtime
        .filterNot(_.getName matches "scala-library.*\\.jar")
        .filterNot(_.getName matches "scalajs-library.*\\.jar")
        .filterNot(_.getName matches "scala3-library.*\\.jar")



    val compileDependencies = compileDependencyJarFiles
      .flatMap(BoringStuff.jarFileToDependency(scalaVersion, log))
    log.debug(s"Compile depends on:\n${compileDependencies.mkString("  ", "\n  ", "")}")

    compileDependencies
  }

  private def getDeclaredCompileDependencies(libraryDependencies: Seq[ModuleID], scalaVersion: ScalaVersion, log: Logger): Set[Dependency] = {
    val compileConfigLibraryDependencies = libraryDependencies
      .filter(isCompileDependency)
      .filterNot(_.name == "scala-library")
      .filterNot(_.name == "scalajs-library")
      .filterNot(_.name == "scala3-library")


    val declaredCompileDependencies = 
      compileConfigLibraryDependencies
        .map(BoringStuff.moduleIDToDependency)

    log.debug(s"Declared dependencies:\n${declaredCompileDependencies.mkString("  ", "\n  ", "")}")

    declaredCompileDependencies.toSet
  }

  private def isCompileDependency(moduleID: ModuleID): Boolean = {
    // Couldn't find any definitive documentation on what format sbt supports for the configuration string,
    // but the Ivy docs are a helpful reference: https://ant.apache.org/ivy/history/2.3.0/tutorial/conf.html
    // As far as I can tell, configs are separated by a semicolon with an optional space,
    // and a single config can be written in two ways:
    // - "compile", meaning the compile config of this project depends on the default config of the library
    // - "test->compile", meaning the test config of this project depends on the compile config of the library
    // So a full config string could be "compile->compile; test->test".
    // We care whether the compile config of this project has a dependency on any config of the library.
    moduleID.configurations.fold[Boolean](true)(conf => conf.split("; ?")
      .exists(c => c.startsWith("compile") || c.startsWith("provided") || c.startsWith("optional")))
  }


  private def toModuleID(dep: Dependency): ModuleID = ModuleID(
    organization = dep.organization,
    name = dep.name,
    revision = dep.version
  )

}
