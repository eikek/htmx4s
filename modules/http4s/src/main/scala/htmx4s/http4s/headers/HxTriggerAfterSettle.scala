package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.constants.HtmxResponseHeaders
import org.http4s.Header
import org.http4s.ParseResult

final case class HxTriggerAfterSettle(name: String)

object HxTriggerAfterSettle:
  val name: CIString = CIString(HtmxResponseHeaders.hxTriggerAfterSettle)

  given Header[HxTriggerAfterSettle, Header.Single] =
    Header.create(name, _.name, s => ParseResult.success(HxTriggerAfterSettle(s)))
