package htmx4s.example

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object Params:

  object Query extends OptionalQueryParamDecoderMatcher[String]("q")
