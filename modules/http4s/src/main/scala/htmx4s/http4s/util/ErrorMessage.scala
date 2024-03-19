package htmx4s.http4s.util

import cats.data.NonEmptyList

trait ErrorMessage[K, M]:
  def key: K
  def messages: NonEmptyList[M]

object ErrorMessage:
  def apply[K,M](key: K, messages: NonEmptyList[M]): ErrorMessage[K,M] =
    Impl(key, messages)

  def of[K, M](key: K, message: M, messages: M*): ErrorMessage[K, M] =
    Impl[K,M](key, NonEmptyList(message, messages.toList))

  private class Impl[K, M](val key: K, val messages: NonEmptyList[M])
      extends ErrorMessage[K, M]
