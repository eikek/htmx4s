package htmx4s.example.contacts

import org.http4s.HttpRoutes
import org.http4s.headers.Location
import org.http4s.implicits.*
import org.http4s.scalatags.*

import cats.effect.*
import cats.syntax.all.*

import htmx4s.example.contacts.Model.*
import htmx4s.example.contacts.Views.notFoundPage
import htmx4s.example.lib.Model.*
import htmx4s.http4s.Htmx4sDsl
import htmx4s.http4s.headers.HxTrigger

final class Routes[F[_]: Async](api: RoutesApi[F]) extends Htmx4sDsl[F]:
  def routes: HttpRoutes[F] = HttpRoutes.of:
    case req @ GET -> Root / "contacts" :? Params.Query(q) +& Params.Page(p) =>
      for {
        result <- api.search(q, p)
        resp <- req.headers
          .get[HxTrigger]
          .whenIn(Views.searchControls)(
            Ok(Views.contactTable(result, p.getOrElse(1))),
            Ok(
              Views.contactListPage(
                ContactListPage(result, q.filter(_.nonEmpty), p.getOrElse(1))
              )
            )
          )
      } yield resp

    case GET -> Root / "contacts" / "count" =>
      api.countAll.flatMap(n => Ok(Views.countSnippet(n)))

    case GET -> Root / "contacts" / "new" =>
      Ok(Views.editContactPage(ContactEditPage.empty))

    case req @ POST -> Root / "contacts" / "new" =>
      for {
        formInput <- req.as[ContactEditForm]
        result <- api.upsert(formInput, None)
        resp <- result.fold(Ok(Views.notFoundPage))(
          _.fold(
            errs =>
              Ok(Views.editContactPage(ContactEditPage(None, formInput, errs.some))),
            _ => SeeOther(Location(uri"/ui/contacts"))
          )
        )
      } yield resp

    case GET -> Root / "contacts" / LongVar(id) =>
      for {
        c <- api.findById(id)
        view = c
          .map(ContactShowPage.apply)
          .fold(Views.notFoundPage)(Views.showContactPage)
        resp <- c.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case GET -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        contact <- api.findById(id)
        form = contact.map(ContactEditForm.from)
        view = form.fold(Views.notFoundPage)(c =>
          Views.editContactPage(ContactEditPage(id.some, c, None))
        )
        resp <- contact.fold(NotFound(view))(_ => Ok(view))
      } yield resp

    case req @ POST -> Root / "contacts" / LongVar(id) / "edit" =>
      for {
        formInput <- req.as[ContactEditForm]
        result <- api.upsert(formInput, id.some)
        resp <- result.fold(Ok(Views.notFoundPage))(
          _.fold(
            errs =>
              Ok(Views.editContactPage(ContactEditPage(id.some, formInput, errs.some))),
            _ => SeeOther(Location(uri"/ui/contacts"))
          )
        )
      } yield resp

    case req @ DELETE -> Root / "contacts" =>
      for {
        ids <- req.as[SelectedIds]
        _ <- ids.selectedId.traverse(api.delete)
        all <- api.search(None, None)
        resp <- Ok(Views.contactListPage(ContactListPage(all, None, 1)))
      } yield resp

    case DELETE -> Root / "contacts" / LongVar(id) =>
      for {
        found <- api.delete(id)
        resp <-
          if (found) SeeOther(Location(uri"/ui/contacts"))
          else NotFound(notFoundPage)
      } yield resp

    case GET -> Root / "contacts" / "email-check" :?
        Params.Email(emailStr) +& Params.IdOpt(id) =>
      for {
        result <- api.checkMail(id, emailStr)
        resp <- result.fold(
          errs => Ok(Views.errorList(errs.some, ContactError.Key.Email)),
          _ => Ok(Views.errorList(Nil))
        )
      } yield resp
