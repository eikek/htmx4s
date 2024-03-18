package htmx4s.scalatags

import scalatags.text
import scalatags.Text

object Bundle extends Text.Cap with text.Tags with text.Tags2 with Text.Aggregate:
  object css extends Text.Cap with Text.Styles with Text.Styles2
  object attr extends Text.Cap with Text.Attrs with HtmxAttributes[text.Builder, String, String]
