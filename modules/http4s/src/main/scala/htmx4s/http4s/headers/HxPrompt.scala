package htmx4s.http4s.headers

import htmx4s.scalatags.HtmxRequestHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.typelevel.ci.CIString

final case class HxPrompt(value: String)

object HxPrompt:
  val name: CIString = CIString(HtmxRequestHeaders.hxPrompt.value)

  given Header[HxPrompt, Header.Single] =
    Header.create(name, _.value, s => ParseResult.success(HxPrompt(s)))
