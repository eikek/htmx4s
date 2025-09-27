package htmx4s.example.lib

import htmx4s.example.lib.Model.Email
import htmx4s.example.lib.Model.Name
import htmx4s.example.lib.Model.PhoneNumber

import doobie.*

private trait DoobieInstances:
  given Meta[Email] =
    Meta[String].tiemap(s => Email(s).toEither.left.map(_.toList.mkString(", ")))(_.value)

  given Meta[PhoneNumber] =
    Meta[String]
      .tiemap(s => PhoneNumber(s).toEither.left.map(_.toList.mkString(", ")))(_.value)

  given Read[Name] =
    Read[(String, String)].map { case (fn, ln) =>
      Name.create(fn, ln).fold(err => sys.error(err.toList.mkString(", ")), identity)
    }
