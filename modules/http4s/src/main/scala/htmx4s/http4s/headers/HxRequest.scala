package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.scalatags.HtmxRequestHeaders
import org.http4s.Header

final case class HxRequest(flag: Boolean)

object HxRequest extends FlagHeader:
  val name: CIString = CIString(HtmxRequestHeaders.hxRequest.value)

  given Header[HxRequest, Header.Single] =
    Header.create(name, _.flag.toString, s => parseFlag(s).map(HxRequest.apply))
