package htmx4s.http4s.headers

import htmx4s.constants.HtmxResponseHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.http4s.Uri
import org.http4s.headers.Location
import org.typelevel.ci.CIString

final case class HxRedirect(uri: Uri)

object HxRedirect:
  val name: CIString = CIString(HtmxResponseHeaders.hxRedirect)

  given Header[HxRedirect, Header.Single] =
    Header.create(name, _.uri.renderString, parse)

  def parse(s: String): ParseResult[HxRedirect] =
    Location.parse(s).map(l => HxRedirect(l.uri))
