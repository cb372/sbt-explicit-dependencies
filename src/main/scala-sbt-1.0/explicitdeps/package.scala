import scala.reflect.api.JavaUniverse

package object explicitdeps {

  type Binary = sbt.librarymanagement.Binary
  type Analysis = sbt.internal.inc.Analysis
  type ModuleFilter = sbt.librarymanagement.ModuleFilter

  val defaultModuleFilter: ModuleFilter = sbt.librarymanagement.DependencyFilter.moduleFilter()

  private val ru: JavaUniverse = scala.reflect.runtime.universe
  private val rm: ru.Mirror = ru.runtimeMirror(getClass.getClassLoader)

  private def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]

  private def toFile(x: AnyRef, typeTag: String, csrCacheDirectoryValueOpt: Option[String]): java.io.File = {
    typeTag match {
      // sbt 1.4.0 or newer
      case "xsbti.VirtualFileRef" =>
        // TODO: Use reflection to invoke id() function
        val path = x.asInstanceOf[xsbti.VirtualFile].id().replaceAllLiterally("${CSR_CACHE}", csrCacheDirectoryValueOpt.mkString)
        new java.io.File(path)
      // sbt 1.3.x or older
      case "java.io.File" =>
        x.asInstanceOf[java.io.File]
    }
  }

  def getAllLibraryDeps(analysis: Analysis, log: sbt.util.Logger)(csrCacheDirectoryValueOpt: Option[String]): Set[java.io.File] = {
    log.debug(
      s"Source to library relations:\n${analysis.relations.libraryDep.all.map(r => s"  ${r._1} -> ${r._2}").mkString("\n")}"
    )
    val allLibraryDeps = analysis.relations.allLibraryDeps.map { x =>
      val typeTag = getTypeTag(x).tpe.toString
      toFile(x, typeTag, csrCacheDirectoryValueOpt)
    }.toSet
    log.debug(s"Library dependencies:\n${allLibraryDeps.mkString("  ", "\n  ", "")}")
    allLibraryDeps
  }

}
