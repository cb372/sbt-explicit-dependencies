import cats.data.NonEmptyList
import cats.free.Free

object Main {

  println(NonEmptyList.of(1, 2, 3))

  sealed trait KVStoreA[A]
  case class Put[T](key: String, value: T) extends KVStoreA[Unit]
  case class Get[T](key: String) extends KVStoreA[Option[T]]
  case class Delete(key: String) extends KVStoreA[Unit]

  type KVStore[A] = Free[KVStoreA, A]

  def put[T](key: String, value: T): KVStore[Unit] =
    Free.liftF[KVStoreA, Unit](Put[T](key, value))
}
