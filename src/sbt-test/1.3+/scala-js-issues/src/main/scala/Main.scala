import com.raquo.laminar.api.L._

import org.scalajs.dom

object Main {
  val app = div("hello")

  def main(args: Array[String]): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("appContainer"), app)
    }(unsafeWindowOwner)
  }
}
