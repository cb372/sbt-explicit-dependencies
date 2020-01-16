import cats.data.NonEmptyList
import shapeless._

object Core {
  println(NonEmptyList.of(1, 2, 3))
  println(1 :: "a" :: HNil)
}
