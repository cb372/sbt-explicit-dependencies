scalaVersion := "2.12.6"
libraryDependencies ++= Seq(
  "software.amazon.cryptools" % "AmazonCorrettoCryptoProvider" % "1.1.0" classifier "linux-x86_64"
)
