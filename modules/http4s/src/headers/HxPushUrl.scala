package htmx4s.http4s.headers

import org.http4s.Header
import org.http4s.ParseResult
import org.http4s.Uri
import org.http4s.headers.Location
import org.typelevel.ci.CIString

import htmx4s.constants.HtmxResponseHeaders

final case class HxPushUrl(value: Option[Uri]):
  private def render: String = value match
    case None    => "false"
    case Some(u) => u.renderString

object HxPushUrl:
  val name: CIString = CIString(HtmxResponseHeaders.hxPushUrl)

  given Header[HxPushUrl, Header.Single] =
    Header.create(name, _.render, parse)

  def parse(s: String): ParseResult[HxPushUrl] =
    if ("false".equalsIgnoreCase(s)) ParseResult.success(HxPushUrl(None))
    else Location.parse(s).map(l => HxPushUrl(Some(l.uri)))
