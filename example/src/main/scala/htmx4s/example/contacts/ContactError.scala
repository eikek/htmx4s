package htmx4s.example.contacts

import htmx4s.http4s.util.ErrorMessage
import cats.data.NonEmptyList
import htmx4s.http4s.util.ValidationErrors
import cats.data.Validated

object ContactError:
  opaque type Key = String

  object Key:
    extension (self: Key)
      def value: String = self
      def msg(m1: String, mn: String*): ErrorMessage[Key, String] =
        ErrorMessage(self, NonEmptyList(m1, mn.toList))

    val default: Key = "*default*"
    val email: Key = "email"
    val phone: Key = "phone"
    val firstName: Key = "firstName"
    val lastName: Key = "lastName"
    val name: Key = "name"

  type Errors = ValidationErrors[Key, String]
  type ContactValid[A] = Validated[Errors, A]
