package htmx4s.example.contacts

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object Params:

  object Query extends OptionalQueryParamDecoderMatcher[String]("q")

  object Page extends OptionalQueryParamDecoderMatcher[Int]("page")

  object Email extends QueryParamDecoderMatcher[String]("email")
