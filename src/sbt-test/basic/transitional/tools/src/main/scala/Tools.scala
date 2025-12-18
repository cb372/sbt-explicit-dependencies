import cats.data.NonEmptyList
import scala.meta._

object Tools {
  println(NonEmptyList.of(1, 2, 3))
  println(Term.Name("Foo"))
}
