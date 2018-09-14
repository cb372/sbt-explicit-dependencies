package explicitdeps

import sbt.Keys._
import sbt._
import sbt.internal.inc.Analysis

object ExplicitDepsPlugin extends AutoPlugin {

  object UndeclaredCompileDependenciesException extends FeedbackProvidedException {
    override def toString: String = "Failing the build because undeclared depedencies were found"
  }

  object autoImport {
    val undeclaredCompileDependencies = taskKey[Set[Dependency]]("find all libraries that this project's code directly depends on for compilation, but which are not declared in libraryDependencies")
    val undeclaredCompileDependenciesTest = taskKey[Unit]("fail the build if there are any libraries that have not been explicitly declared as compile-time dependencies")
  }
  import autoImport._

  override def trigger = allRequirements
  override def requires = empty
  override lazy val projectSettings = Seq(
    undeclaredCompileDependencies := undeclaredCompileDependenciesTask.value,
    undeclaredCompileDependenciesTest := undeclaredCompileDependenciesTestTask.value
  )

  lazy val undeclaredCompileDependenciesTask = Def.task {
    val log = streams.value.log

    val compileAnalysis =
      compile.in(Compile).value.asInstanceOf[Analysis]

    val compileDependencyJarFiles =
      compileAnalysis.relations.allLibraryDeps
        .filter(_.getName.endsWith(".jar"))
        .filterNot(_.getName == "rt.jar") // Java runtime
        .filterNot(_.getName == "scala-library.jar")

    val compileDependencies = compileDependencyJarFiles
      .flatMap(BoringStuff.jarFileToDependency(scalaBinaryVersion.value, log))
    log.debug(s"Compile depends on:\n${compileDependencies.mkString("\n")}")

    val compileConfigLibraryDependencies = libraryDependencies.value
      .filter(_.configurations.fold[Boolean](true)(_.contains("compile")))

    val declaredCompileDependencies = compileConfigLibraryDependencies
      .map(moduleId => Dependency(moduleId.organization, moduleId.name, moduleId.revision, moduleId.crossVersion == Binary()))
    log.debug(s"Declared dependencies:\n${declaredCompileDependencies.mkString("\n")}")

    val undeclaredCompileDependencies = compileDependencies.toSet diff declaredCompileDependencies.toSet
    if (undeclaredCompileDependencies.nonEmpty) {
      val sorted = undeclaredCompileDependencies.toList.sortBy(dep => s"${dep.organization} ${dep.name}")
      log.warn(
        s"""The project depends on the following libraries for compilation but they are not declared in libraryDependencies:
          |${sorted.mkString("\n")}""".stripMargin)
    } else {
      log.info("The project explicitly declares all the libraries that it directly depends on for compilation. Good job!")
    }

    undeclaredCompileDependencies
  }

  lazy val undeclaredCompileDependenciesTestTask = Def.task {
    val undeclaredCompileDeps = undeclaredCompileDependencies.value
    if (undeclaredCompileDeps.nonEmpty)
      throw UndeclaredCompileDependenciesException
  }
}
