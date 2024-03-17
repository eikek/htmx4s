package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.scalatags.HtmxRequestHeaders
import org.http4s.Header
import org.http4s.ParseResult

final case class HxTriggerName(name: String)

object HxTriggerName:
  val name: CIString = CIString(HtmxRequestHeaders.hxTriggerName.value)

  given Header[HxTriggerName, Header.Single] =
    Header.create(name, _.name, s => ParseResult.success(HxTriggerName(s)))
