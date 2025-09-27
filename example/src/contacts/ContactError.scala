package htmx4s.example.contacts

import cats.data.Validated
import cats.syntax.all.*

import htmx4s.http4s.util.ValidationErrors

object ContactError:
  enum Key:
    case Default
    case Email
    case Phone
    case FirstName
    case LastName
    case Name

  def emailExists[A]: ContactValid[A] = ValidationErrors
    .one(Key.Email, s"Email already exists!")
    .invalid

type Errors = ValidationErrors[ContactError.Key, String]
type ContactValid[A] = Validated[Errors, A]
