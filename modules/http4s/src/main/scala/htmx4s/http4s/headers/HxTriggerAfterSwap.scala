package htmx4s.http4s.headers

import htmx4s.constants.HtmxResponseHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.typelevel.ci.CIString

final case class HxTriggerAfterSwap(name: String)

object HxTriggerAfterSwap:
  val name: CIString = CIString(HtmxResponseHeaders.hxTriggerAfterSwap)

  given Header[HxTriggerAfterSwap, Header.Single] =
    Header.create(name, _.name, s => ParseResult.success(HxTriggerAfterSwap(s)))
