package htmx4s.example.contacts

import cats.effect.kernel.Sync
import cats.syntax.all.*

import htmx4s.example.contacts.Model.*
import htmx4s.example.lib.ContactDb
import htmx4s.example.lib.ContactDb.UpdateResult
import htmx4s.example.lib.Model.*
import htmx4s.http4s.util.ValidationDsl.*

trait RoutesApi[F[_]]:
  def upsert(
      form: ContactEditForm,
      id: Option[Long]
  ): F[ContactValid[Long]]
  def checkMail(id: Option[Long], emailStr: String): F[ContactValid[Email]]
  def delete(id: Long): F[Boolean]
  def findById(id: Long): F[Option[Contact]]
  def search(query: Option[String], page: Option[Int]): F[List[Contact]]

object RoutesApi:
  def apply[F[_]: Sync](db: ContactDb[F]): RoutesApi[F] =
    new RoutesApi[F] {
      def search(query: Option[String], page: Option[Int]): F[List[Contact]] =
        db.search(query, page)
      def findById(id: Long): F[Option[Contact]] = db.findById(id)
      def delete(id: Long): F[Boolean] = db.delete(id)
      def upsert(
          form: ContactEditForm,
          id: Option[Long]
      ): F[ContactValid[Long]] =
        form
          .toContact(id.getOrElse(-1L))
          .fold(
            _.invalid.pure[F],
            c =>
              db.upsert(c).map {
                case UpdateResult.Success(id)    => id.valid
                case UpdateResult.EmailDuplicate => ContactError.emailExists
              }
          )

      def checkMail(id: Option[Long], emailStr: String): F[ContactValid[Email]] =
        Email(emailStr)
          .keyed(ContactError.Key.Email)
          .fold(
            _.invalid.pure[F],
            email =>
              db.findByEmail(email).map {
                case Some(c) if id.forall(_ != c.id) => ContactError.emailExists
                case _                            => email.valid
              }
          )
    }
