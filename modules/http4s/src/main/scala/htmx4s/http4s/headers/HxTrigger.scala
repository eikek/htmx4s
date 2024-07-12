package htmx4s.http4s.headers

import htmx4s.constants.HtmxRequestHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.typelevel.ci.CIString

final case class HxTrigger(name: String)

object HxTrigger:
  val name: CIString = CIString(HtmxRequestHeaders.hxTrigger)

  given Header[HxTrigger, Header.Single] =
    Header.create(name, _.name, s => ParseResult.success(HxTrigger(s)))
