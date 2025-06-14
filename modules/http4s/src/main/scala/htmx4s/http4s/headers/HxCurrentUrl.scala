package htmx4s.http4s.headers

import org.http4s.Header
import org.http4s.ParseResult
import org.http4s.Uri
import org.typelevel.ci.CIString

import htmx4s.constants.HtmxRequestHeaders

final case class HxCurrentUrl(url: Uri)

object HxCurrentUrl:
  val name: CIString = CIString(HtmxRequestHeaders.hxCurrentURL)

  given Header[HxCurrentUrl, Header.Single] =
    Header.create(name, _.url.renderString, parse)

  def parse(s: String): ParseResult[HxCurrentUrl] =
    Uri.fromString(s).map(HxCurrentUrl.apply)
