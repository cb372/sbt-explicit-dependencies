package explicitdeps

import sbt.Keys._
import sbt._

object ExplicitDepsPlugin extends AutoPlugin {

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
    val allLibraryDeps = compile.in(Compile).value.asInstanceOf[Analysis].relations.allLibraryDeps.toSet
    val libraryDeps = libraryDependencies.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val log = streams.value.log

    Logic.getUndeclaredCompileDependencies(
      projectName,
      allLibraryDeps,
      libraryDeps,
      scalaBinaryVer,
      log
    )
  }

  lazy val undeclaredCompileDependenciesTestTask = Def.task {
    val undeclaredCompileDeps = undeclaredCompileDependencies.value
    if (undeclaredCompileDeps.nonEmpty)
      throw UndeclaredCompileDependenciesException
  }

  lazy val unusedCompileDependenciesTask = Def.task {
    val projectName = name.value
    val allLibraryDeps = compile.in(Compile).value.asInstanceOf[Analysis].relations.allLibraryDeps.toSet
    val libraryDeps = libraryDependencies.value
    val scalaBinaryVer = scalaBinaryVersion.value
    val log = streams.value.log

    Logic.getUnusedCompileDependencies(
      projectName,
      allLibraryDeps,
      libraryDeps,
      scalaBinaryVer,
      log
    )
  }

  lazy val unusedCompileDependenciesTestTask = Def.task {
    val unusedCompileDeps = unusedCompileDependencies.value
    if (unusedCompileDeps.nonEmpty)
      throw UnusedCompileDependenciesException
  }

}
