package htmx4s.example.contacts

import cats.effect.*
import cats.syntax.all.*

import htmx4s.example.contacts.Model.ContactEditForm
import htmx4s.example.contacts.Model.*
import htmx4s.example.contacts.Views.notFoundPage
import htmx4s.example.lib.ContactDb
import htmx4s.example.lib.ContactDb.UpdateResult
import htmx4s.example.lib.Model.*
import htmx4s.http4s.Htmx4sDsl
import htmx4s.http4s.util.ValidationDsl.*
import htmx4s.http4s.util.ValidationErrors

import org.http4s.HttpRoutes
import org.http4s.headers.Location
import org.http4s.implicits.*
import org.http4s.scalatags.*

// TODO:
// - accept / content-type negotiation
// - derive formdecoder

final class Routes[F[_]: Async](db: ContactDb[F]) extends Htmx4sDsl[F]:
  val emailExistsError = ValidationErrors
    .one(ContactError.Key.email, s"Email already exists!")
    .invalid

  def upsert(
      form: ContactEditForm,
      id: Option[Long]
  ): F[ContactError.ContactValid[Long]] =
    form
      .toContact(id.getOrElse(-1L))
      .fold(
        _.invalid.pure[F],
        c =>
          db.upsert(c).map {
            case UpdateResult.Success(id)    => id.valid
            case UpdateResult.EmailDuplicate => emailExistsError
          }
      )

  def checkMail(emailStr: String): F[ContactError.ContactValid[Email]] =
    Email(emailStr)
      .keyed(ContactError.Key.email)
      .fold(
        _.invalid.pure[F],
        email =>
          db.findByEmail(email).map {
            case Some(_) => emailExistsError
            case None    => email.valid
          }
      )

  def routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "contacts" :? Params.Query(q) +& Params.Page(p) =>
      for {
        result <- db.search(q, p)
        _ <- Async[F].blocking(println(s"result: $result"))
        view = Views.contactListPage(
          ContactListPage(result, q.filter(_.nonEmpty), p.getOrElse(1))
        )
        resp <- Ok(view)
      } yield resp

    case GET -> Root / "contacts" / "new" =>
      Ok(Views.editContactPage(ContactEditPage.empty))

    case req @ POST -> Root / "contacts" / "new" =>
      for {
        formInput <- req.as[ContactEditForm]
        result <- upsert(formInput, None)
        resp <- result.fold(
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
        result <- upsert(formInput, id.some)
        resp <- result.fold(
          errs =>
            Ok(Views.editContactPage(ContactEditPage(id.some, formInput, errs.some))),
          _ => SeeOther(Location(uri"/ui/contacts"))
        )
      } yield resp

    case DELETE -> Root / "contacts" / LongVar(id) =>
      for {
        found <- db.delete(id)
        resp <-
          if (found) SeeOther(Location(uri"/ui/contacts"))
          else NotFound(notFoundPage)
      } yield resp

    case GET -> Root / "contacts" / "email-check" :? Params.Email(emailStr) =>
      for {
        result <- checkMail(emailStr)
        resp <- result.fold(
          errs => Ok(Views.errorList(errs.some, ContactError.Key.email)),
          _ => Ok(Views.errorList(Nil))
        )
      } yield resp
  }
