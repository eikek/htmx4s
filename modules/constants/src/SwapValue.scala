package htmx4s.constants

final case class SwapValue(style: SwapStyle, modifiers: Seq[SwapModifier] = Seq.empty):
  private val modifierStr = modifiers.map(_.render).mkString(" ").trim match
    case "" => ""
    case s  => s" $s"
  val render: String = s"${style.render}${modifierStr}"

object SwapValue:
  def of(style: SwapStyle, mods: SwapModifier*): SwapValue =
    SwapValue(style, mods)

  def parse(s: String): Either[String, SwapValue] =
    val (style, rest) = s.span(!_.isWhitespace)
    val modifiers = rest.trim
      .split("\\s+")
      .toSeq
      .map(_.trim)
      .map(SwapModifier.fromString)
      .foldLeft(Right(Seq.empty): Either[String, Seq[SwapModifier]]) { (res, eab) =>
        res.flatMap(list => eab.map(e => list :+ e))
      }
    SwapStyle
      .fromString(style)
      .flatMap(style => modifiers.map(mods => SwapValue(style, mods)))
