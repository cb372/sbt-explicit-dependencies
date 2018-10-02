package object explicitdeps {

  type Logger = sbt.util.Logger
  type ModuleID = sbt.librarymanagement.ModuleID
  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis

  def getAllLibraryDeps(analysis: Analysis): Set[java.io.File] =
    analysis.relations.allLibraryDeps.toSet

}
