package explicitdeps

final case class Dependency(organization: String, name: String, version: String, crossVersion: Boolean) {

  override def toString: String =
    if (crossVersion)
      s""""$organization" %% "$name" % "$version""""
    else
      s""""$organization" % "$name" % "$version""""

}


