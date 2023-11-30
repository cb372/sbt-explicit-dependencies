import scalaz._
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.option._
import scalaz.syntax.equal._ 

object Main {
  val list1: List[Option[Int]] = List(Some(1), Some(2), Some(3), Some(4))
  assert(Traverse[List].sequence(list1) === Some(List(1,2,3,4)))
}
