
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object Main extends App {

  @JsonIgnoreProperties(Array("b"))
  case class Foo(a: Int, b: String, c: Boolean)

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val foo = Foo(123, "abc", true)
  val json = mapper.writeValueAsString(foo)
  println(json)
  assert(json == """{"a":123,"c":true}""")

}
