package htmx4s.constants

enum ScrollPosition:
  case Top
  case Bottom

object ScrollPosition:
  def fromString(s: String): Either[String, ScrollPosition] =
    if (s.equalsIgnoreCase("top")) Right(Top)
    else if (s.equalsIgnoreCase("bottom")) Right(Bottom)
    else Left(s"Invalid position (neither top nor bottom): $s")
