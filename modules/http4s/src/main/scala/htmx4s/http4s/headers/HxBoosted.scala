package htmx4s.http4s.headers

import htmx4s.scalatags.HtmxRequestHeaders

import org.http4s.Header
import org.typelevel.ci.CIString

final case class HxBoosted(flag: Boolean)

object HxBoosted extends FlagHeader:
  val name: CIString = CIString(HtmxRequestHeaders.hxBoosted.value)

  given Header[HxBoosted, Header.Single] =
    Header.create(name, _.flag.toString, s => parseFlag(s).map(HxBoosted.apply))
