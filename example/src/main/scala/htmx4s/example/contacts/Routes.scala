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
import htmx4s.example.contacts.Model.ContactEditForm
import htmx4s.example.contacts.Views.notFoundPage

// TODO:
// - accept / content-type negotiation
// - derive formdecoder

final class Routes[F[_]: Async](db: ContactDb[F]) extends Htmx4sDsl[F]:

  def routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "contacts" :? Params.Query(q) =>
      for {
        result <- db.search(q)
        view = Views.contactListPage(ContactListPage(result, q))
        resp <- Ok(view, `Cache-Control`(CacheDirective.`no-cache`()))
      } yield resp

    case GET -> Root / "contacts" / "new" =>
      Ok(Views.editContactPage(ContactEditPage.empty))

    case req @ POST -> Root / "contacts" / "new" =>
      for {
        formInput <- req.as[ContactEditForm]
        contact = formInput.toContact(-1L)
        _ <- contact.fold(_ => ().pure[F], c => db.upsert(c).void)
        resp <- contact.fold(
          errs => Ok(Views.editContactPage(ContactEditPage(None, formInput, errs.some))),
          _ => SeeOther(Location(uri"/ui/contacts"))
        )
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
        contact <- db.findById(id)
        form = contact.map(ContactEditForm.from)
        view = form.fold(Views.notFoundPage)(c =>
          Views.editContactPage(ContactEditPage(id.some, c, None))
        )
        resp <- contact.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case req @ POST -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        formInput <- req.as[ContactEditForm]
        contact = formInput.toContact(id)
        _ <- contact.fold(_ => ().pure[F], c => db.upsert(c.copy(id = id)).void)
        resp <- contact.fold(
          errs =>
            Ok(Views.editContactPage(ContactEditPage(id.some, formInput, errs.some))),
          _ => SeeOther(Location(uri"/ui/contacts"))
        )
      } yield resp

    case POST -> Root / "contacts" / LongVar(id) / "delete" =>
      for {
        found <- db.delete(id)
        resp <-
          if (found) SeeOther(Location(uri"/ui/contacts"))
          else NotFound(notFoundPage)
      } yield resp
  }
