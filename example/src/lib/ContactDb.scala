package htmx4s.example.lib

import cats.effect.*
import cats.syntax.all.*

import htmx4s.example.lib.Model.*

trait ContactDb[F[_]]:
  def search(query: Option[String], page: Option[Int]): F[List[Contact]]
  def delete(id: Long): F[Boolean]
  def upsert(contact: Contact): F[ContactDb.UpdateResult]
  def findById(id: Long): F[Option[Contact]]
  def findByEmail(email: Email): F[Option[Contact]]
  def count: F[Long]

object ContactDb:
  enum UpdateResult:
    case Success(id: Long)
    case NotFound
    case EmailDuplicate

  def apply[F[_]: Async]: Resource[F, ContactDb[F]] =
    H2Database.create[F]().map { db =>
      new ContactDb[F]:
        def search(query: Option[String], page: Option[Int]): F[List[Contact]] =
          val q = query.map(_.toLowerCase).map(e => s"%$e%")
          val skip = (page.getOrElse(1) - 1) * 10
          db.selectContacts(q, 10, skip)

        def count: F[Long] = db.countAll

        def delete(id: Long): F[Boolean] =
          db.delete(id)

        def upsert(contact: Contact): F[UpdateResult] =
          contact.email
            .flatTraverse(db.findByEmail)
            .flatMap:
              case Some(existing) if contact.id > 0 =>
                if (existing.id != contact.id) UpdateResult.EmailDuplicate.pure[F]
                else
                  db.update(contact)
                    .map:
                      case true  => UpdateResult.Success(contact.id)
                      case false => UpdateResult.NotFound
              case None if contact.id > 0 =>
                db.update(contact)
                  .map:
                    case true  => UpdateResult.Success(contact.id)
                    case false => UpdateResult.NotFound
              case Some(_) =>
                UpdateResult.EmailDuplicate.pure[F]
              case None =>
                db.insert(contact).map(UpdateResult.Success(_))

        def findByEmail(email: Email): F[Option[Contact]] =
          db.findByEmail(email)

        def findById(id: Long): F[Option[Contact]] =
          db.findById(id)
    }
