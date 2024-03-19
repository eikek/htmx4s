package htmx4s.example.contacts

import cats.effect.*
import cats.syntax.all.*

import htmx4s.example.contacts.Model.*
import htmx4s.example.lib.ContactDb
import htmx4s.example.lib.Model.*
import htmx4s.http4s.Htmx4sDsl

import org.http4s.CacheDirective
import org.http4s.HttpRoutes
import org.http4s.headers.Location
import org.http4s.headers.`Cache-Control`
import org.http4s.implicits.*
import org.http4s.scalatags.*

// TODO:
// - separate view model and lib model (view encodes app state)
// - accept / content-type negotiation
// - derive formdecoder

final class Routes[F[_]: Async](db: ContactDb[F]) extends Htmx4sDsl[F] with ModelDecoder:

  def routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "contacts" :? Params.Query(q) =>
      for {
        result <- db.search(q)
        view = Views.contactListPage(ContactListPage(result, q))
        resp <- Ok(view, `Cache-Control`(CacheDirective.`no-cache`()))
      } yield resp

    case GET -> Root / "contacts" / "new" =>
      Ok(Views.editContact(None))

    case req @ POST -> Root / "contacts" / "new" =>
      for {
        c <- req.as[Contact]
        _ <- db.upsert(c)
        resp <- SeeOther(Location(uri"/ui/contacts"))
      } yield resp

    case GET -> Root / "contacts" / LongVar(id) =>
      for {
        c <- db.findById(id)
        view = c
          .map(ContactShowPage.apply)
          .fold(Views.notFoundPage)(Views.showContactPage)
        resp <- c.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case GET -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        c <- db.findById(id)
        view = c.fold(Views.notFoundPage)(c =>
          Views.editContactPage(ContactEditPage(Some(c)))
        )
        resp <- c.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case req @ POST -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        c <- req.as[Contact]
        _ <- db.upsert(c.copy(id = id))
        resp <- SeeOther(Location(uri"/ui/contacts"))
      } yield resp
  }
