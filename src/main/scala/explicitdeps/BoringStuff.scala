package explicitdeps

import java.io.File

import scala.util.control.NonFatal
import scala.xml.XML

import sbt.util.Logger

case class ScalaVersion(
  binary: String,
  full: String
)

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
      // We use URL decoder to cater for characters that are valid in version strings but encoded
      // in the file path, such as "+" used for semver metadata, encoded as "%2B"
      val version = java.net.URLDecoder.decode(
        file.getParentFile.getName,
        java.nio.charset.Charset.forName("utf8")
      )

      val (name, crossVersion) = parseModuleName(scalaVersion)(rawName)

      Some(Dependency(organization, name, version, crossVersion))
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

      val (name, crossVersion) = parseModuleName(scalaVersion)(rawName)

      Some(Dependency(organization, name, version, crossVersion))
    } catch {
      case NonFatal(e) =>
        log.warn(s"Failed to parse dependency information from Ivy file ${file.getAbsolutePath}")
        None
    }
  }

  private def parseModuleName(scalaVersion: ScalaVersion)(rawName: String): (String, Boolean) =
    if (rawName.endsWith(s"_${scalaVersion.binary}"))
      (rawName.replaceAllLiterally(s"_${scalaVersion.binary}", ""), true)
    else if (rawName.endsWith(s"_${scalaVersion.full}"))
      (rawName.replaceAllLiterally(s"_${scalaVersion.full}", ""), true)
    else
      (rawName, false)

}
