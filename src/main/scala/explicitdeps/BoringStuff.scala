package explicitdeps

import java.io.File

import scala.util.control.NonFatal
import scala.xml.XML

import sbt.util.Logger
import sbt.librarymanagement.ModuleID

case class ScalaVersion(
  binary: String,
  full: String
) {
  def binarySuffix = "_" + binary
  def fullSuffix = "_" + full
}

object BoringStuff {

  /*
  Zinc helpfully tells us which libraries the compile depends on, but unfortunately
  it only gives us the absolute path of the jar file.
  We need to reverse-engineer this into a "dependency" containing info about
  the organisation, module name and version.
   */
  def jarFileToDependency(scalaVersion: ScalaVersion, log: Logger)(jarFile: File): Option[Dependency] = {
    val dependencyFromPom = findPomFile(jarFile).flatMap(parsePomFile(scalaVersion, log))
    val dependencyFromIvyCache = findIvyFileInIvyCache(jarFile).flatMap(parseIvyFile(scalaVersion, log))
    val dependencyFromIvyLocal = findIvyFileInIvyLocal(jarFile).flatMap(parseIvyFile(scalaVersion, log))

    val dependencyOpt = dependencyFromPom.orElse(dependencyFromIvyCache).orElse(dependencyFromIvyLocal)
    log.debug(s"jarFile: ${jarFile.getName} -> ${dependencyOpt.mkString}")

    dependencyOpt
  }

  private def findPomFile(jarFile: File): Option[File] = {
    // pom file should be in the same directory as the jar, with the same filename but a .pom extension
    val filenames = jarFile.getName.dropRight(4).split('-').inits
      .filter(_.nonEmpty).map(_.mkString("-") + ".pom").toList
    val pomFiles = filenames.map(filename => new File(jarFile.getParentFile, filename))
    pomFiles.find(_.exists)
  }

  private def findIvyFileInIvyCache(jarFile: File): Option[File] = {
    // Ivy file should be in the parent directory, with the filename ivy-$version.xml
    val artifactVersion = jarFile.getName.dropRight(4).split('-').tail
    val potentialVersions = (artifactVersion.tails.toList.reverse ++ artifactVersion.inits.toList.tail)
      .filter(_.nonEmpty).map(_.mkString("-"))
    val potentialIvyFiles = potentialVersions.map(version => new File(jarFile.getParentFile.getParentFile, s"ivy-$version.xml"))
    potentialIvyFiles.find(_.exists)
  }

  private def findIvyFileInIvyLocal(jarFile: File): Option[File] = {
    // Jar file will be in 'jars' directory. Ivy file should be in the sibling 'ivys' directory, with the filename ivy.xml
    val ivysDirectory = new File(jarFile.getParentFile.getParentFile, "ivys")
    Some(new File(ivysDirectory, "ivy.xml")).filter(_.exists)
  }

  private def parsePomFile(scalaVersion: ScalaVersion, log: Logger)(file: File): Option[Dependency] = {
    try {
      val xml = XML.loadFile(file)
      val organization = {
        val groupId = (xml \ "groupId").text
        if (groupId.nonEmpty) groupId else (xml \ "parent" \ "groupId").text
      }

      val rawName = (xml \ "artifactId").text

      // We use the parent dir to get the version because it's sometimes not present in the pom file
      val version = file.getParentFile.getName

      val (name, crossVersion, platform) = parseModuleName(scalaVersion)(rawName)

      Some(Dependency(organization, name, version, crossVersion, platform))
    } catch {
      case NonFatal(e) =>
        log.warn(s"Failed to parse dependency information from POM file ${file.getAbsolutePath}")
        None
    }
  }

  private def parseIvyFile(scalaVersion: ScalaVersion, log: Logger)(file: File): Option[Dependency] = {
    try {
      val xml = XML.loadFile(file)
      val organization = xml \ "info" \@ "organisation"
      val rawName = xml \ "info" \@ "module"
      val version = xml \ "info" \@ "revision"

      val (name, crossVersion, scalajs) = parseModuleName(scalaVersion)(rawName)

      Some(Dependency(organization, name, version, crossVersion, scalajs))
    } catch {
      case NonFatal(e) =>
        log.warn(s"Failed to parse dependency information from Ivy file ${file.getAbsolutePath}")
        None
    }
  }

  private def parseModuleName(scalaVersion: ScalaVersion)(rawName: String): (String, Boolean, Option[ScalaJSVersion]) = {
    if (rawName.endsWith(scalaVersion.binarySuffix)) {
      val name = rawName.dropRight(scalaVersion.binarySuffix.length)
      val platform = parseScalaPlatform(name)
      val newName = name.dropRight(platform.map(_.suffix.length).getOrElse(0))
      (name, true, platform)
    }
    else if (rawName.endsWith(scalaVersion.fullSuffix))
      (rawName.dropRight(scalaVersion.fullSuffix.length), true, None)
    else
      (rawName, false, None)
  }
  
  private def parseScalaPlatform(rawName: String): Option[ScalaJSVersion] = 
    if(rawName.endsWith(ScalaJSVersion.V1.suffix)) Some(ScalaJSVersion.V1)
    else if (rawName.endsWith(ScalaJSVersion.V06.suffix)) Some(ScalaJSVersion.V06)
    else None


  def moduleIDToDependency(moduleId: ModuleID): Dependency = {
    val isCross = moduleId.crossVersion.isInstanceOf[Binary] ||  moduleId.crossVersion.isInstanceOf[Full]
    val platform = moduleId.crossVersion match {
      case b: Binary if b.prefix == ScalaJSVersion.V1.prefix => Some(ScalaJSVersion.V1)
      case b: Binary if b.prefix == ScalaJSVersion.V06.prefix => Some(ScalaJSVersion.V06)
      case _ => None
    }

    Dependency(
      moduleId.organization, 
      moduleId.name + platform.map(_.suffix).getOrElse(""), 
      moduleId.revision, 
      isCross, 
      platform
    )
  }
}
