import cats.data.NonEmptyList
import cats.Functor
import cats.instances.either._

object Main {
  println(NonEmptyList.of(1, 2, 3))

  val F = implicitly[Functor[Either[String, ?]]]
  println(F.map(Right(123))(_ + 1))
}
