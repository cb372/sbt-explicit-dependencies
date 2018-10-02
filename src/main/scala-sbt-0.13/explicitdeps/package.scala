package object explicitdeps {

  type Logger = sbt.Logger
  type ModuleID = sbt.ModuleID
  type Binary = sbt.CrossVersion.Binary
  type Analysis = sbt.inc.Analysis

  def getAllLibraryDeps(analysis: Analysis): Set[java.io.File] =
    analysis.relations.allBinaryDeps.toSet

  implicit class NodeSeqOps(nodeSeq: scala.xml.NodeSeq) {

    def \@(attributeName: String): String = (nodeSeq \ ("@" + attributeName)).text

  }
}
