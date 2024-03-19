package htmx4s.example

import cats.data.ValidatedNel
import cats.syntax.all.*

import htmx4s.http4s.util.Valid
import htmx4s.example.lib.Model.*

import org.http4s.FormDataDecoder.*
import org.http4s.*

trait LibModelDecoder:
  given emailQueryDecoder: QueryParamDecoder[Valid[Option[Email]]] =
    QueryParamDecoder.stringQueryParamDecoder.map { s =>
      if (s.isEmpty()) Valid.valid(None)
      else Valid.from(ErrorKey.email, Email(s)).map(Some(_))
    }

  given phoneQueryDecoder: QueryParamDecoder[Valid[Option[PhoneNumber]]] =
    QueryParamDecoder.stringQueryParamDecoder.map(s => ???)

  given nameDecoder: FormDataDecoder[Valid[Name]] =
    (field[String]("first"), field[String]("last")).mapN(Name.create)

  given contactDecoder: FormDataDecoder[Valid[Contact]] =
    (
      nested[Valid[Name]]("name"),
      field[Valid[Option[PhoneNumber]]]("phone"),
      field[Valid[Option[Email]]]("email")
    )
      .mapN((a, b, c) => (a, b, c).mapN(Contact.withoutId))
