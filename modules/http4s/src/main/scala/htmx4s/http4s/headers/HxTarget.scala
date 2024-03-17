package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.scalatags.HtmxRequestHeaders
import org.http4s.Header
import org.http4s.ParseResult

final case class HxTarget(id: String)

object HxTarget:
  val name: CIString = CIString(HtmxRequestHeaders.hxTarget.value)

  given Header[HxTarget, Header.Single] =
    Header.create(name, _.id, s => ParseResult.success(HxTarget(s)))
