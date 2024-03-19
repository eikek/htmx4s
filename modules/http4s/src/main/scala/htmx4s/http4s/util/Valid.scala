package htmx4s.http4s.util

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all.*
import cats.Applicative

opaque type ValidKM[K, M, A] = Validated[ValidationErrors[K, M], A]

type Valid[A] = ValidKM[String, String, A]

object ValidKM:
  def valid[K, M, A](a: A): ValidKM[K, M, A] = Validated.valid(a)

  def from[K, M, A](key: K, vm: ValidatedNel[M, A]): ValidKM[K, M, A] =
    vm.leftMap(ms => ValidationErrors.of(ErrorMessage(key, ms)))

  def when[K, M, A](
      cond: Boolean,
      a: => A,
      err: => ErrorMessage[K, M]
  ): ValidKM[K, M, A] =
    if (cond) Validated.valid(a) else Validated.invalid(ValidationErrors.of(err))

  extension [K, M, A](self: ValidKM[K, M, A])
    def onSuccessIgnorError[F[_]: Applicative](action: A => F[Unit]): F[Unit] =
      self.fold(_ => ().pure[F], action)

    def toValidatedNel: ValidatedNel[M, A] =
      self.leftMap(_.errors.map(_.messages).reduce(_ concatNel _))

    private def selfAgain = self
    export selfAgain.{toValidatedNel => _, *}

val Valid = ValidKM
