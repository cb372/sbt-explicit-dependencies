package object explicitdeps {

  type Binary = sbt.CrossVersion.Binary
  type Analysis = sbt.inc.Analysis
  type ModuleFilter = sbt.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.DependencyFilter.moduleFilter()

  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger): Set[java.io.File] = {
    log.debug(
      s"Library dependency relations:\n${analysis.relations.binaryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    analysis.relations.allBinaryDeps.toSet
  }

  implicit class NodeSeqOps(nodeSeq: scala.xml.NodeSeq) {

    def \@(attributeName: String): String = (nodeSeq \ ("@" + attributeName)).text

  }
}
