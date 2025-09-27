package htmx4s.http4s.headers

import org.http4s.Header
import org.http4s.ParseFailure
import org.typelevel.ci.CIString

import cats.data.NonEmptyList

sealed trait Vary

object Vary:
  val name: CIString = CIString("Vary")

  val all: Vary = All

  def of(name: CIString, names: CIString*): Vary =
    Headers(NonEmptyList(name, names.toList))

  def of[T](using h: Header[T, ?]): Vary =
    of(h.name)

  case object All extends Vary
  case class Headers(headers: NonEmptyList[CIString]) extends Vary

  private def render(v: Vary): String = v match
    case All         => "*"
    case Headers(hs) => hs.toList.map(_.toString).mkString(", ")

  def parse(s: String): Either[ParseFailure, Vary] =
    if ("*" == s) Right(all)
    else
      val segs = s.split(',').filter(_.nonEmpty).map(e => CIString(e.trim))
      NonEmptyList
        .fromList(segs.toList)
        .map(Headers.apply)
        .toRight(ParseFailure("Invalid Vary header value", s))

  given Header[Vary, Header.Single] =
    Header.create(name, render, parse)
