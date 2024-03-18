package htmx4s.constants

enum SwapStyle(val render: String):
  case InnerHtml extends SwapStyle("innerHTML")
  case OuterHtml extends SwapStyle("outerHTML")
  case BeforeBegin extends SwapStyle("beforebegin")
  case BeforeEnd extends SwapStyle("beforeend")
  case AfterEnd extends SwapStyle("afterend")
  case Delete extends SwapStyle("delete")
  case None extends SwapStyle("none")

object SwapStyle:
  def fromString(s: String): Either[String, SwapStyle] =
    SwapStyle.values.find(_.render.equalsIgnoreCase(s)).toRight(s"Invalid swap style: $s")
