package htmx4s.example

import cats.data.NonEmptyList
import htmx4s.http4s.util.*

opaque type ErrorKey = String

object ErrorKey:
  extension (self: ErrorKey)
    def msg(m1: String, mn: String*): ErrorMessage[ErrorKey, String] =
      ErrorMessage(self, NonEmptyList(m1, mn.toList))

  val email: ErrorKey = "email"
  val phone: ErrorKey = "phone"
  val firstName: ErrorKey = "firstName"
  val lastName: ErrorKey = "lastName"
