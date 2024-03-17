package htmx4s.http4s.headers

import org.http4s.headers.Location
import org.http4s.Uri
import org.typelevel.ci.CIString
import htmx4s.scalatags.HtmxResponseHeaders
import org.http4s.Header

final case class HxLocation(value: HxLocation.Value)

object HxLocation:
  val name: CIString = CIString(HtmxResponseHeaders.hxLocation.value)

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
      s => Location.parse(s).map(l => HxLocation(Value.Path(l.uri))) //TODO
    )
