import cats.data.NonEmptyList
import shapeless3.typeable.Typeable

object Core {
  println(NonEmptyList.of(1, 2, 3))
  println(Typeable[String].cast(42) :: Typeable[Int].cast(42) :: Nil)
}
