package htmx4s.http4s.headers

import htmx4s.constants.HtmxRequestHeaders

import org.http4s.Header
import org.typelevel.ci.CIString

final case class HxRequest(flag: Boolean)

object HxRequest extends FlagHeader:
  val name: CIString = CIString(HtmxRequestHeaders.hxRequest)

  given Header[HxRequest, Header.Single] =
    Header.create(name, _.flag.toString, s => parseFlag(s).map(HxRequest.apply))
