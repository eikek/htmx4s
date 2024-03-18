package htmx4s.example

import cats.syntax.all.*
import org.http4s.*
import org.http4s.FormDataDecoder.*


trait ModelDecoder:
  given QueryParamDecoder[Option[String]] =
    QueryParamDecoder.stringQueryParamDecoder.map(s => Option(s).filter(_.nonEmpty))

  given FormDataDecoder[Model.Name] =
    (field[String]("first"), field[String]("last")).mapN(Model.Name.apply)

  given FormDataDecoder[Model.Contact] =
    (
      nested[Model.Name]("name"),
      field[Option[String]]("phone"),
      field[Option[String]]("email")
    )
      .mapN(Model.Contact.withoutId)
