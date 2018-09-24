package explicitdeps

import java.io.File

object Logic {

  def getUndeclaredCompileDependencies(
    projectName: String,
    allLibraryDeps: Set[File],
    libraryDeps: Seq[ModuleID],
    scalaBinaryVer: String,
    log: Logger
  ): Set[Dependency] = {
    val compileDependencies = getCompileDependencies(allLibraryDeps, scalaBinaryVer, log)
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

  def getUnusedCompileDependencies(
    projectName: String,
    allLibraryDeps: Set[File],
    libraryDeps: Seq[ModuleID],
    scalaBinaryVer: String,
    log: Logger
  ): Set[Dependency] = {
    val compileDependencies = getCompileDependencies(allLibraryDeps, scalaBinaryVer, log)
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

  private def getCompileDependencies(allLibraryDeps: Set[File], scalaBinaryVersion: String, log: Logger): Set[Dependency] = {
    val compileDependencyJarFiles =
      allLibraryDeps
        .filter(_.getName.endsWith(".jar"))
        .filterNot(_.getName == "rt.jar") // Java runtime
        .filterNot(_.getName matches "scala-library.*\\.jar")

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
      .map(moduleId => Dependency(moduleId.organization, moduleId.name, moduleId.revision, moduleId.crossVersion.isInstanceOf[Binary]))
    log.debug(s"Declared dependencies:\n${declaredCompileDependencies.mkString("\n")}")

    declaredCompileDependencies.toSet
  }

}
