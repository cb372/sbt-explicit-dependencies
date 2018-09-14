package foo

import cats.effect.IO

case class MyCaseClass(io: IO[Int])
