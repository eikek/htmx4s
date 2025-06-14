package htmx4s.http4s.headers

import org.http4s.Header
import org.http4s.ParseResult
import org.typelevel.ci.CIString

import htmx4s.constants.HtmxResponseHeaders

final case class HxRetarget(selector: String)

object HxRetarget:
  val name: CIString = CIString(HtmxResponseHeaders.hxRetarget)

  given Header[HxRetarget, Header.Single] =
    Header.create(name, _.selector, s => ParseResult.success(HxRetarget(s)))
