package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import htmx4s.scalatags.HtmxRequestHeaders
import org.http4s.Header

final case class HxHistoryRestoreRequest(flag: Boolean)

object HxHistoryRestoreRequest extends FlagHeader:
  val name: CIString = CIString(HtmxRequestHeaders.hxHistoryRestoreRequest.value)

  given Header[HxHistoryRestoreRequest, Header.Single] =
    Header.create(
      name,
      _.flag.toString,
      s => parseFlag(s).map(HxHistoryRestoreRequest.apply)
    )
