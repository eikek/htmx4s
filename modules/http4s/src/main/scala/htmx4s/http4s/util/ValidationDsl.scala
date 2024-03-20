package htmx4s.http4s.util

import cats.kernel.Monoid
import cats.data.{Validated, ValidatedNel}

object ValidationDsl extends ValidationDsl
trait ValidationDsl:

  extension [A](self: A)
    def keyed[K](key: K): ErrorMessage[K, A] =
      ErrorMessage.of(key, self)

    def valid[K,M]: Validated[ValidationErrors[K,M], A] = Validated.valid(self)

    def emptyOption(using m: Monoid[A]): Option[A] =
      if (m.empty == self) None else Some(self)

    def asNonEmpty[K, M](key: K, msg: M)(using
        m: Monoid[A]
    ): Validated[ValidationErrors[K, M], A] =
      if (m.empty == self) Validated.invalid(ValidationErrors.one(key, msg))
      else Validated.valid(self)

  extension [A, M](self: ValidatedNel[M, A])
    def keyed[K](key: K): Validated[ValidationErrors[K, M], A] =
      self.leftMap(errs => ValidationErrors.of(key, errs))
