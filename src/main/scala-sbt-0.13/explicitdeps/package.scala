package object explicitdeps {

  type Binary = sbt.CrossVersion.Binary
  type Analysis = sbt.inc.Analysis
  type ModuleFilter = sbt.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.DependencyFilter.moduleFilter()

  def getAllLibraryDeps(analysis: Analysis): Set[java.io.File] =
    analysis.relations.allBinaryDeps.toSet

  implicit class NodeSeqOps(nodeSeq: scala.xml.NodeSeq) {

    def \@(attributeName: String): String = (nodeSeq \ ("@" + attributeName)).text

  }
}
