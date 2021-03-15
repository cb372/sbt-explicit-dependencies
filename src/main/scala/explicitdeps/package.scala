import sbt.librarymanagement.ModuleID
package object explicitdeps {

  type Binary = sbt.librarymanagement.Binary
  type Full = sbt.librarymanagement.Full
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  private def toFile(x: AnyRef, csrCacheDirectoryValueOpt: Option[String], baseDirectoryValue: String): java.io.File = {
    if (x.getClass.getSimpleName.contains("VirtualFile")) {
      // sbt 1.4.0 or newer
      val id = x.getClass.getMethod("id").invoke(x).toString
      val path = id
        .replaceAllLiterally("${CSR_CACHE}", csrCacheDirectoryValueOpt.mkString)
        .replaceAllLiterally("${BASE}", baseDirectoryValue)
      new java.io.File(path)
    } else {
      // sbt 1.3.x or older
      x.asInstanceOf[java.io.File]
    }
  }

  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger)
    (csrCacheDirectoryValueOpt: Option[String], baseDirectoryValue: String): Set[java.io.File] = {
    log.debug(
      s"Source to library relations:\n${analysis.relations.libraryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    log.debug(s"Using CSR_CACHE=${csrCacheDirectoryValueOpt.mkString} BASE=$baseDirectoryValue")
    val allLibraryDeps = analysis.relations.allLibraryDeps.asInstanceOf[Set[AnyRef]]
      .map(x => toFile(x, csrCacheDirectoryValueOpt, baseDirectoryValue))
      .toSet
    log.debug(s"Library dependencies:\n${allLibraryDeps.mkString("  ", "\n  ", "")}")
    allLibraryDeps
  }
  
  def modulePlatform(moduleId: ModuleID): Option[ScalaJSVersion] = 
    moduleId.crossVersion match {
      case b: sbt.librarymanagement.Binary => 
        if(b.prefix == ScalaJSVersion.V1.prefix)
          Some(ScalaJSVersion.V1)
        else if (b.prefix == ScalaJSVersion.V06.prefix)
          Some(ScalaJSVersion.V06)
        else None
      case _ => None
    }

}
