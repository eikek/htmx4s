package htmx4s.http4s.headers

import org.http4s.Header
import org.http4s.ParseFailure
import org.typelevel.ci.CIString

import htmx4s.constants.*

final case class HxReswap(value: SwapValue)

object HxReswap:
  val name: CIString = CIString(HtmxResponseHeaders.hxReswap)

  given Header[HxReswap, Header.Single] =
    Header.create(
      name,
      _.value.render,
      s => SwapValue.parse(s).left.map(err => ParseFailure(s, err)).map(HxReswap.apply)
    )
