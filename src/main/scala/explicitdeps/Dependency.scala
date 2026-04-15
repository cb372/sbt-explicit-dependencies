package explicitdeps

import sjsonnew.BasicJsonProtocol._
import sjsonnew.JsonFormat

final case class Dependency(organization: String, name: String, version: String, crossVersion: Boolean) {

  override def toString: String =
    if (crossVersion)
      s""""$organization" %% "$name" % "$version""""
    else
      s""""$organization" % "$name" % "$version""""

}

object Dependency {
  implicit val dependencyJsonFormat: JsonFormat[Dependency] =
    caseClassArray4(
      Dependency.apply,
      (d: Dependency) => Some((d.organization, d.name, d.version, d.crossVersion))
    )
}
