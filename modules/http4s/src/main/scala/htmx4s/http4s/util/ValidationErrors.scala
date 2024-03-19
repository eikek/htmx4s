package htmx4s.http4s.util

import cats.data.NonEmptyList
import cats.Semigroup

final case class ValidationErrors[K, M](
    errors: NonEmptyList[ErrorMessage[K, M]]
):
  def find(key: K): Option[NonEmptyList[M]] =
    NonEmptyList
      .fromList(errors.filter(_.key == key).map(_.messages))
      .map(nels => nels.tail.foldLeft(nels.head)(_ concatNel _))

  def findList(key: K): List[M] = find(key).map(_.toList).getOrElse(Nil)

object ValidationErrors:
  def of[K, M](em: ErrorMessage[K, M], ems: ErrorMessage[K, M]*): ValidationErrors[K, M] =
    ValidationErrors(NonEmptyList(em, ems.toList))

  def of[K, M](key: K, messages: NonEmptyList[M]): ValidationErrors[K, M] =
    ValidationErrors.of(ErrorMessage(key, messages))

  given catsSemigroup[K, M]: Semigroup[ValidationErrors[K, M]] =
    Semigroup.instance((a, b) => ValidationErrors(a.errors.concatNel(b.errors)))
