package htmx4s.http4s

import cats.effect.*
import cats.syntax.all.*

import org.http4s.*
import org.http4s.dsl.Http4sDsl
import cats.Functor
import htmx4s.http4s.headers.Vary
import htmx4s.http4s.headers.HxTrigger

trait Htmx4sDsl[F[_]] extends Http4sDsl[F]:

  given formEntityDecoder[A](using
      F: Concurrent[F],
      fdd: FormDataDecoder[A]
  ): EntityDecoder[F, A] =
    FormDataDecoder.formEntityDecoder[F, A]

  extension [F[_]: Functor](self: F[Response[F]])
    def vary[H](using Header[H, ?]): F[Response[F]] =
      self.map(_.putHeaders(Vary.of[H]))

    def varyHxTrigger: F[Response[F]] =
      vary[HxTrigger]

  extension [F[_]](self: Response[F])
    def vary[H](using Header[H, ?]): Response[F] =
      self.putHeaders(Vary.of[H])

    def varyHxTrigger: Response[F] =
      vary[HxTrigger]

  extension [F[_]: Functor](self: Option[HxTrigger])
    def when(f: Option[String] => F[Response[F]]): F[Response[F]] =
      f(self.map(_.name)).varyHxTrigger

    def cond(
        c: String => Boolean
    )(ifTrue: => F[Response[F]], ifFalse: F[Response[F]]): F[Response[F]] =
      when(n => if (n.exists(c)) ifTrue else ifFalse)

    def whenIn(
        values: Set[String]
    )(ifTrue: => F[Response[F]], ifFalse: F[Response[F]]): F[Response[F]] =
      cond(values.contains)(ifTrue, ifFalse)
