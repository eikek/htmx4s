package htmx4s.scalatags

import scalatags.Text
import scalatags.text

trait Bundle
    extends Text.Cap
    with text.Tags
    with text.Tags2
    with Text.Aggregate
    with HtmxTagValues:
  object css extends Text.Cap with Text.Styles with Text.Styles2
  object attr
      extends Text.Cap
      with Text.Attrs
      with HtmxAttributes[text.Builder, String, String]

  val cls = attr.cls

  def hxOn(event: String): Attr = attr(s"hx-on:$event")

  extension (sc: StringContext) def hxOn(args: Any*): Attr = attr(s"hx-on:${sc.s(args*)}")

object Bundle extends Bundle
