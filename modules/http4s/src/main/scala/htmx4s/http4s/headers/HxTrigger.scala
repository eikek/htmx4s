package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.scalatags.HtmxRequestHeaders
import org.http4s.Header
import org.http4s.ParseResult

final case class HxTrigger(name: String)

object HxTrigger:
  val name: CIString = CIString(HtmxRequestHeaders.hxTrigger.value)

  given Header[HxTrigger, Header.Single] =
    Header.create(name, _.name, s => ParseResult.success(HxTrigger(s)))
