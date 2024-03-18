package htmx4s.http4s

import cats.effect.*
import org.http4s.dsl.Http4sDsl
import org.http4s.FormDataDecoder
import org.http4s.EntityDecoder

trait Htmx4sDsl[F[_]] extends Http4sDsl[F]:

  given formEntityDecoder[A](using
      F: Concurrent[F],
      fdd: FormDataDecoder[A]
  ): EntityDecoder[F, A] =
    FormDataDecoder.formEntityDecoder[F, A]
