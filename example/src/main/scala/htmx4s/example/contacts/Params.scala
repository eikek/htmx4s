package htmx4s.example.contacts

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object Params:

  object Query extends OptionalQueryParamDecoderMatcher[String]("q")
