package explicitdeps

private[explicitdeps] sealed abstract class ScalaJSVersion(val suffix: String, val prefix: String)
private[explicitdeps] object ScalaJSVersion {
  case object V1 extends ScalaJSVersion("_sjs1", "sjs1_")
  case object V06 extends ScalaJSVersion("_sjs0.6", "sjs0.6_")
}

final case class Dependency(organization: String, name: String, version: String, crossVersion: Boolean, scalaJS: Option[ScalaJSVersion]) {

  override def toString: String = {
    val platformDimension = if(scalaJS.isDefined) "%%%" else "%%"
    val nameWithoutPlatform = scalaJS match { 
      case Some(sjs) => name.dropRight(sjs.suffix.length())  
      case None => name
    } 

    if (crossVersion)
      s""""$organization" $platformDimension "$nameWithoutPlatform" % "$version""""
    else
      s""""$organization" % "$name" % "$version""""
  }

}


