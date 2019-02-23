package object explicitdeps {

  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  def getAllLibraryDeps(analysis: Analysis): Set[java.io.File] =
    analysis.relations.allLibraryDeps.toSet

}
