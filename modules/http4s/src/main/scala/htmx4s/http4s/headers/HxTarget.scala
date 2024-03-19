package htmx4s.http4s.headers

import htmx4s.constants.HtmxRequestHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.typelevel.ci.CIString

final case class HxTarget(id: String)

object HxTarget:
  val name: CIString = CIString(HtmxRequestHeaders.hxTarget)

  given Header[HxTarget, Header.Single] =
    Header.create(name, _.id, s => ParseResult.success(HxTarget(s)))
