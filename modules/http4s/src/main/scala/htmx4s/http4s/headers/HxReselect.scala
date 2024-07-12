package htmx4s.http4s.headers

import htmx4s.constants.HtmxResponseHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.typelevel.ci.CIString

final case class HxReselect(selector: String)

object HxReselect:
  val name: CIString = CIString(HtmxResponseHeaders.hxReselect)

  given Header[HxReselect, Header.Single] =
    Header.create(name, _.selector, s => ParseResult.success(HxReselect(s)))
