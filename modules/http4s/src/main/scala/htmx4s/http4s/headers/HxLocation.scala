package htmx4s.http4s.headers

import htmx4s.constants.HtmxResponseHeaders

import org.http4s.Header
import org.http4s.ParseResult
import org.http4s.Uri
import org.typelevel.ci.CIString

/** HX-Location response header. */
final case class HxLocation(value: HxLocation.Value)

object HxLocation:
  val name: CIString = CIString(HtmxResponseHeaders.hxLocation)

  enum Value:
    case Path(uri: Uri)
    case Context(ctx: LocationContext)

    def render: String = this match
      case Path(uri)    => uri.renderString
      case Context(ctx) => ctx.render

  def parse(value: String): ParseResult[HxLocation] =
    if (value.startsWith("{"))
      LocationContext.parse(value).map(Value.Context.apply).map(HxLocation.apply)
    else Uri.fromString(value).map(Value.Path.apply).map(HxLocation.apply)

  given Header[HxLocation, Header.Single] =
    Header.create(name, _.value.render, parse)
