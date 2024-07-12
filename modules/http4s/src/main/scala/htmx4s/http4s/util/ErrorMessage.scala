package htmx4s.http4s.util

import cats.data.NonEmptyList

final case class ErrorMessage[K, M](key: K, messages: NonEmptyList[M]):
  def map[MM](f: M => MM): ErrorMessage[K, MM] =
    ErrorMessage(key, messages.map(f))

object ErrorMessage:
  def of[K, M](key: K, message: M, messages: M*): ErrorMessage[K, M] =
    ErrorMessage[K, M](key, NonEmptyList(message, messages.toList))
