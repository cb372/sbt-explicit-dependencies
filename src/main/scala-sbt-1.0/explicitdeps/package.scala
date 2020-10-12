package object explicitdeps {

  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger)(csrCacheDirectoryValueOpt: Option[String]): Set[java.io.File] = {
    log.debug(
      s"Source to library relations:\n${analysis.relations.libraryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    val allLibraryDeps = analysis.relations.allLibraryDeps
      .map(_.id())
      .map(_.replaceAllLiterally("${CSR_CACHE}", csrCacheDirectoryValueOpt.mkString))
      .map(path => new java.io.File(path))
      .toSet
    log.debug(s"Library dependencies:\n${allLibraryDeps.mkString("  ", "\n  ", "")}")
    allLibraryDeps
  }

}
