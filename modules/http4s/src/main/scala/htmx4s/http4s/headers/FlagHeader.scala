package htmx4s.http4s.headers

import org.typelevel.ci.CIString
import org.http4s.ParseResult

private[headers] trait FlagHeader:
  def name: CIString

  private[headers] def parseFlag(s: String) = s.trim.toLowerCase match
    case "true" => ParseResult.success(true)
    case "false" => ParseResult.success(false)
    case _ => ParseResult.fail(s, s"Invalid $name header value: $s")
