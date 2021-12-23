package object explicitdeps {

  type Binary = sbt.CrossVersion.Binary
  type Full = sbt.CrossVersion.Full
  type Analysis = sbt.inc.Analysis
  type ModuleFilter = sbt.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.DependencyFilter.moduleFilter()

  // csrCacheDirectoryValueOpt, baseDirectoryValue are unused and are only present for forward compatibility
  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger)
    (csrCacheDirectoryValueOpt: Option[String], baseDirectoryValue: String, ivyHomeValue: String): Set[java.io.File] = {
    log.debug(
      s"Source to library relations:\n${analysis.relations.binaryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    val allLibraryDeps = analysis.relations.allBinaryDeps.toSet
    log.debug(s"Library dependencies:\n${allLibraryDeps.mkString("  ", "\n  ", "")}")
    allLibraryDeps
  }

  implicit class NodeSeqOps(nodeSeq: scala.xml.NodeSeq) {

    def \@(attributeName: String): String = (nodeSeq \ ("@" + attributeName)).text

  }
}
