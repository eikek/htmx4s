package htmx4s.example.contacts

import cats.syntax.all.*
import htmx4s.http4s.util.ErrorMessage
import cats.data.{NonEmptyList, ValidatedNel}
import htmx4s.http4s.util.ValidationErrors
import cats.data.Validated
import cats.Applicative

object ContactError:
  opaque type Key = String

  object Key:
    extension (self: Key)
      def value: String = self
      def msg(m1: String, mn: String*): ErrorMessage[Key, String] =
        ErrorMessage(self, NonEmptyList(m1, mn.toList))

    val email: Key = "email"
    val phone: Key = "phone"
    val name: Key = "name"

  type Errors = ValidationErrors[Key, String]
  type ContactValid[A] = Validated[Errors, A]

  object ContactValid:
    def valid[A](a: A): ContactValid[A] =
      Validated.valid(a)

  extension [A](self: Validated[ValidationErrors[Key, String], A])
    def onSuccessIgnorError[F[_]: Applicative](action: A => F[Unit]): F[Unit] =
      self.fold(_ => ().pure[F], action)

  extension [A](self: ValidatedNel[String, A])
    def toContactValid(key: Key): ContactValid[A] =
      self.leftMap(errs => ValidationErrors.of(key, errs))
