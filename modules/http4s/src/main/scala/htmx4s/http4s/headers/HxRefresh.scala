package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.constants.HtmxResponseHeaders
import org.http4s.Header

final case class HxRefresh(flag: Boolean)

object HxRefresh extends FlagHeader:
  val name: CIString = CIString(HtmxResponseHeaders.hxRefresh)

  given Header[HxRefresh, Header.Single] =
    Header.create(name, _.flag.toString, s => parseFlag(s).map(HxRefresh.apply))
