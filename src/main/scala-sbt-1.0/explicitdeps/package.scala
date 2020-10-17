package object explicitdeps {

  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  private def toFile(x: AnyRef, className: String, csrCacheDirectoryValueOpt: Option[String]): java.io.File = {
    if (className.contains("VirtualFile")) {
      // sbt 1.4.0 or newer
      val id = x.toString
      val path = id.replaceAllLiterally("${CSR_CACHE}", csrCacheDirectoryValueOpt.mkString)
      new java.io.File(path)
    } else {
      // sbt 1.3.x or older
      x.asInstanceOf[java.io.File]
    }
  }

  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger)(csrCacheDirectoryValueOpt: Option[String]): Set[java.io.File] = {
    log.debug(
      s"Source to library relations:\n${analysis.relations.libraryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    val allLibraryDeps = analysis.relations.allLibraryDeps.asInstanceOf[Set[AnyRef]].map { x =>
      val className = x.getClass.getSimpleName
      toFile(x, className, csrCacheDirectoryValueOpt)
    }.toSet
    log.debug(s"Library dependencies:\n${allLibraryDeps.mkString("  ", "\n  ", "")}")
    allLibraryDeps
  }

}
