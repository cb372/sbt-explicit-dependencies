package object explicitdeps {

  type Logger = sbt.Logger
  type ModuleID = sbt.ModuleID
  type Binary = sbt.CrossVersion.Binary
  type Analysis = sbt.inc.Analysis
  type ModuleFilter = sbt.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.DependencyFilter.moduleFilter()

  def toModuleID(dep: Dependency): ModuleID = sbt.ModuleID(
    organization = dep.organization,
    name = dep.name,
    revision = dep.version
  )

  def getAllLibraryDeps(analysis: Analysis): Set[java.io.File] =
    analysis.relations.allBinaryDeps.toSet

  implicit class NodeSeqOps(nodeSeq: scala.xml.NodeSeq) {

    def \@(attributeName: String): String = (nodeSeq \ ("@" + attributeName)).text

  }
}
