package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.constants.HtmxResponseHeaders
import org.http4s.Header
import org.http4s.ParseResult

final case class HxTriggerAfterSwap(name: String)

object HxTriggerAfterSwap:
  val name: CIString = CIString(HtmxResponseHeaders.hxTriggerAfterSwap)

  given Header[HxTriggerAfterSwap, Header.Single] =
    Header.create(name, _.name, s => ParseResult.success(HxTriggerAfterSwap(s)))
