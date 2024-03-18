package htmx4s.example

import cats.syntax.all.*
import cats.effect.*
import org.http4s.HttpRoutes
import org.http4s.scalatags.*
import org.http4s.implicits.*
import htmx4s.http4s.Htmx4sDsl
import org.http4s.headers.Location
import org.http4s.headers.`Cache-Control`
import org.http4s.CacheDirective

// TODO:
// - separate view model and lib model (view encodes app state)
// - accept / content-type negotiation
// - derive formdecoder

final class RestRoutes[F[_]: Async](db: Lib.ContactDb[F])
    extends Htmx4sDsl[F]
    with ModelDecoder:

  def routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "contacts" :? Params.Query(q) =>
      for {
        result <- db.search(q)
        view = Views.contactListPage(result, q)
        resp <- Ok(view, `Cache-Control`(CacheDirective.`no-cache`()))
      } yield resp

    case GET -> Root / "contacts" / "new" =>
      Ok(Views.editContact(None))

    case req @ POST -> Root / "contacts" / "new" =>
      for {
        c <- req.as[Model.Contact]
        _ <- db.upsert(c)
        resp <- SeeOther(Location(uri"/ui/contacts"))
      } yield resp

    case GET -> Root / "contacts" / LongVar(id) =>
      for {
        c <- db.findById(id)
        view = c.fold(Views.notFoundPage)(Views.showContactPage)
        resp <- c.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case GET -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        c <- db.findById(id)
        view = c.fold(Views.notFoundPage)(c => Views.editContactPage(Some(c)))
        resp <- c.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case req @ POST -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        c <- req.as[Model.Contact]
        _ <- db.upsert(c.copy(id = id))
        resp <- SeeOther(Location(uri"/ui/contacts"))
      } yield resp
  }
