package htmx4s.example.contacts

import cats.syntax.all.*
import htmx4s.example.lib.Model.*
import ContactError.*
import org.http4s.*
import org.http4s.FormDataDecoder.*

trait ModelDecoder:
  given emailQueryDecoder: QueryParamDecoder[ContactValid[Option[Email]]] =
    QueryParamDecoder.stringQueryParamDecoder.map { s =>
      if (s.isEmpty()) ContactValid.valid(None)
      else Email(s).map(Some(_)).toContactValid(Key.email)
    }

  given phoneQueryDecoder: QueryParamDecoder[ContactValid[Option[PhoneNumber]]] =
    QueryParamDecoder.stringQueryParamDecoder.map { s =>
      if (s.isEmpty) ContactValid.valid(None)
      else PhoneNumber(s).map(Some(_)).toContactValid(Key.phone)
    }

  given nameDecoder: FormDataDecoder[ContactValid[Name]] =
    (field[String]("first"), field[String]("last")).mapN(Name.create).map(_.toContactValid(Key.name))

  given contactDecoder: FormDataDecoder[ContactValid[Contact]] =
    (
      nested[ContactValid[Name]]("name"),
      field[ContactValid[Option[PhoneNumber]]]("phone"),
      field[ContactValid[Option[Email]]]("email")
    )
      .mapN((a, b, c) => (a, b, c).mapN(Contact.withoutId))
