package htmx4s.http4s.headers

import htmx4s.constants.HtmxResponseHeaders

import org.http4s.Header
import org.http4s.Uri
import org.http4s.headers.Location
import org.typelevel.ci.CIString

final case class HxLocation(value: HxLocation.Value)

object HxLocation:
  val name: CIString = CIString(HtmxResponseHeaders.hxLocation)

  enum Value:
    case Path(uri: Uri)
    case Context(ctx: LocationContext)

    def render: String = this match
      case Path(uri)    => uri.renderString
      case Context(ctx) => ""

  given Header[HxLocation, Header.Single] =
    Header.create(
      name,
      _.value.render,
      s => Location.parse(s).map(l => HxLocation(Value.Path(l.uri))) // TODO
    )
