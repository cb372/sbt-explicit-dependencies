package object explicitdeps {

  type Logger = sbt.util.Logger
  type ModuleID = sbt.librarymanagement.ModuleID
  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  def toModuleID(dep: Dependency): ModuleID = sbt.librarymanagement.ModuleID(
    organization = dep.organization,
    name = dep.name,
    revision = dep.version
  )

  def getAllLibraryDeps(analysis: Analysis): Set[java.io.File] =
    analysis.relations.allLibraryDeps.toSet

}
