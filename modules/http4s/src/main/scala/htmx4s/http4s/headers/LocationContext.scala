package htmx4s.http4s.headers

import cats.parse.Parser as P
import cats.syntax.all.*

import org.http4s.ParseFailure
import org.http4s.ParseResult
import org.http4s.Uri

final case class LocationContext(
    path: Uri,
    source: Option[String] = None,
    event: Option[String] = None,
    handler: Option[String] = None,
    target: Option[String] = None,
    swap: Option[String] = None,
    values: Option[String] = None,
    headers: Option[String] = None,
    select: Option[String] = None
):
  def render: String = LocationContext.render(this)
  def withSource(v: String) = copy(source = Some(v))
  def withEvent(v: String) = copy(event = Some(v))
  def withHandler(v: String) = copy(handler = Some(v))
  def withTarget(v: String) = copy(target = Some(v))
  def withSwap(v: String) = copy(swap = Some(v))
  def withValues(v: String) = copy(values = Some(v))
  def withHeaders(v: String) = copy(headers = Some(v))
  def withSelect(v: String) = copy(select = Some(v))

object LocationContext:
  private val jsonString = cats.parse.strings.Json.delimited

  def parse(value: String): ParseResult[LocationContext] =
    try
      Parser.locationContext
        .parseAll(value.trim())
        .leftMap(e => ParseFailure("Invalid HxLocation value", e.show))
    catch { case p: ParseFailure => p.asLeft }

  def render(ctx: LocationContext): String =
    def kv(k: String, v: String) = s""""$k":${jsonString.encode(v)}"""
    def kvo(k: String, v: Option[String]) =
      v.map(kv(k, _))
    List(
      "path" -> Some(ctx.path.renderString),
      "source" -> ctx.source,
      "event" -> ctx.event,
      "handler" -> ctx.handler,
      "target" -> ctx.target,
      "swap" -> ctx.swap,
      "values" -> ctx.values,
      "headers" -> ctx.headers,
      "select" -> ctx.select
    ).flatMap(kvo.tupled).mkString("{", ",", "}")

  private object Parser:
    val strValue: P[String] = jsonString.parser
    val ws0 = P.charsWhile0(_.isWhitespace)
    val keys = Set(
      "path",
      "source",
      "event",
      "handler",
      "target",
      "swap",
      "values",
      "headers",
      "select"
    )
    val key = P.char('"') *> P.stringIn(keys) <* P.char('"')
    val keyValue = key ~ ((ws0 ~ P.char(':') ~ ws0).void *> strValue)
    val keyValueList = keyValue
      .repSep((ws0 ~ P.char(',') ~ ws0).void)
      .filter(_.toList.map(_._1).contains("path"))
      .map { pairs =>
        val init = LocationContext(Uri.unsafeFromString("/"))
        pairs.foldLeft(init) { case (cfg, (key, value)) =>
          key match
            case "path"    => cfg.copy(path = Uri.unsafeFromString(value))
            case "source"  => cfg.withSource(value)
            case "event"   => cfg.withEvent(value)
            case "handler" => cfg.withHandler(value)
            case "target"  => cfg.withTarget(value)
            case "swap"    => cfg.withSwap(value)
            case "values"  => cfg.withValues(value)
            case "headers" => cfg.withHeaders(value)
            case "select"  => cfg.withSelect(value)
            case _         => sys.error(s"unknown key: $key, not in $keys")
        }
      }
    val locationContext =
      (P.char('{') ~ ws0) *> keyValueList <* (ws0 ~ P.char('}'))
  end Parser
