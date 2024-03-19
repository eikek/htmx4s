package htmx4s.example

import cats.syntax.all.*
import org.http4s.*
import org.http4s.FormDataDecoder.*
import htmx4s.example.lib.Model.*

trait LibModelDecoder:
  given QueryParamDecoder[Option[String]] =
    QueryParamDecoder.stringQueryParamDecoder.map(s => Option(s).filter(_.nonEmpty))

  given FormDataDecoder[Name] =
    (field[String]("first"), field[String]("last")).mapN(Name.apply)

  given FormDataDecoder[Contact] =
    (
      nested[Name]("name"),
      field[Option[String]]("phone"),
      field[Option[String]]("email")
    )
      .mapN(Contact.withoutId)
