package object explicitdeps {

  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger): Set[java.io.File] = {
    log.debug(
      s"Library dependency relations:\n${analysis.relations.libraryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    analysis.relations.allLibraryDeps.toSet
  }

}
