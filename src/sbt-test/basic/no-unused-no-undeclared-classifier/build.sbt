scalaVersion := sys.props("scala.version")
libraryDependencies ++= Seq(
  "software.amazon.cryptools" % "AmazonCorrettoCryptoProvider" % "1.1.0" classifier "linux-x86_64"
)
