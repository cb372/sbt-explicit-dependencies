package foo

import cats.data._
import cats.instances.either._
import shapeless._
import cats.effect.IO
import org.http4s.server.blaze._

object Example {

  // depends on http4s-blaze-server (explicitly included in libraryDependencies)
  val blazeBuilder = BlazeBuilder[IO]

  // depends on scala-library (automatically added to libraryDependencies)
  val list = List('a, 'b, 'c)

  // depends on cats-core (transitive dependency)
  val nel = NonEmptyList.of(1, 2, 3)

  // depends on shapeless (transitive dependency)
  val hlist = "a" :: 42 :: true :: HNil

  // depends on Guava (a Java lib with a version containing a dash)
  val hash = com.google.common.hash.Hashing.adler32().hashInt(12345)

  // uses the kind-projector compiler plugin
  val eitherMonad = cats.Monad[Either[String, ?]]

}
