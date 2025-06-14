package htmx4s.http4s.headers

import org.http4s.Header
import org.http4s.ParseResult
import org.http4s.Uri
import org.http4s.headers.Location
import org.typelevel.ci.CIString

import htmx4s.constants.HtmxResponseHeaders

final case class HxReplaceUrl(value: Option[Uri]):
  private def render: String = value match
    case None    => "false"
    case Some(u) => u.renderString

object HxReplaceUrl:
  val name: CIString = CIString(HtmxResponseHeaders.hxReplaceUrl)

  given Header[HxReplaceUrl, Header.Single] =
    Header.create(name, _.render, parse)

  def parse(s: String): ParseResult[HxReplaceUrl] =
    if ("false".equalsIgnoreCase(s)) ParseResult.success(HxReplaceUrl(None))
    else Location.parse(s).map(l => HxReplaceUrl(Some(l.uri)))
