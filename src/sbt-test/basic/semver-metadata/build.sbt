scalaVersion := sys.props("scala.version")
resolvers ++= Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.27+33-04a1ea9e-SNAPSHOT"
)
