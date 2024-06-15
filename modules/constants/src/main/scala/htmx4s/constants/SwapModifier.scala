package htmx4s.constants

import scala.concurrent.duration.*
import scala.util.Try

trait SwapModifier:
  def render: String

object SwapModifier:

  case object Transition extends SwapModifier:
    val render = "transition:true"

  final case class Swap(duration: FiniteDuration) extends SwapModifier:
    val render = s"swap:${duration.toMillis}ms"

  final case class Settle(duration: FiniteDuration) extends SwapModifier:
    val render = s"settle:${duration.toMillis}ms"

  case object IgnoreTitle extends SwapModifier:
    val render = s"ignoreTitle:true"

  final case class Show(pos: ScrollPosition, selector: Option[String])
      extends SwapModifier:
    val render = selector match
      case Some(sel) => s"show:${sel}${pos.productPrefix.toLowerCase}"
      case None      => s"show:${pos.productPrefix.toLowerCase}"

  final case class Scroll(pos: ScrollPosition, selector: Option[String])
      extends SwapModifier:
    val render = selector match
      case Some(sel) => s"scroll:${sel}${pos.productPrefix.toLowerCase}"
      case None      => s"scroll:${pos.productPrefix.toLowerCase}"

  final case class FocusScroll(flag: Boolean) extends SwapModifier:
    val render = s"focus-scroll:$flag"

  def fromString(s: String): Either[String, SwapModifier] =
    val (name, rest) = s.span(_ != ':')
    lazy val bool = rest.trim.equalsIgnoreCase("true")
    lazy val duration = Try(Duration(rest.trim)).toEither.left
      .map(_.getMessage)
      .flatMap:
        case fd: FiniteDuration => Right(fd)
        case d                  => Left(s"Not a finite duration: $d")
    lazy val pos = rest.trim.span(_ != ':') match
      case (sel, pos) if pos.isEmpty =>
        ScrollPosition.fromString(sel).map(p => (p, None))
      case (sel, pos) =>
        ScrollPosition.fromString(pos.drop(1)).map(p => (p, Some(sel)))
    name.toLowerCase match
      case "transition"   => Right(Transition)
      case "swap"         => duration.map(Swap.apply)
      case "settle"       => duration.map(Settle.apply)
      case "ignoretitle"  => Right(IgnoreTitle)
      case "show"         => pos.map(t => Show(t._1, t._2))
      case "scroll"       => pos.map(t => Scroll(t._1, t._2))
      case "focus-scroll" => Right(FocusScroll(bool))
      case _              => Left(s"Invalid swap modifier name: $name")
