package object explicitdeps {

  type Logger = sbt.Logger
  type ModuleID = sbt.ModuleID
  type Binary = sbt.CrossVersion.Binary
  type Analysis = sbt.inc.Analysis

  implicit class NodeSeqOps(nodeSeq: scala.xml.NodeSeq) {

    def \@(attributeName: String): String = (nodeSeq \ ("@" + attributeName)).text

  }
}
