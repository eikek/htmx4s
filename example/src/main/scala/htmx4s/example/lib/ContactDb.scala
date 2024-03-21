package htmx4s.example.lib

import cats.effect.*
import cats.syntax.all.*

import htmx4s.example.lib.Model._

trait ContactDb[F[_]]:
  def search(query: Option[String], page: Option[Int]): F[List[Contact]]
  def delete(id: Long): F[Boolean]
  def upsert(contact: Contact): F[ContactDb.UpdateResult]
  def findById(id: Long): F[Option[Contact]]
  def findByEmail(email: Email): F[Option[Contact]]

object ContactDb:
  enum UpdateResult:
    case Success(id: Long)
    case EmailDuplicate

  def apply[F[_]: Sync]: F[ContactDb[F]] =
    val contacts = Ref.of[F, Map[Long, Contact]](Map.empty)
    val idGen = Ref.of[F, Long](0)
    (contacts, idGen).mapN { case (data, ids) =>
      new ContactDb[F] {
        def search(query: Option[String], page: Option[Int]): F[List[Contact]] =
          val q = query.map(_.toLowerCase)
          val skip = (page.getOrElse(1) - 1) * 10
          data.get.map(
            _.values
              .filter(c => q.forall(c.contains))
              .toList
              .sortBy(_.name)
              .drop(skip)
              .take(10)
          )

        def delete(id: Long): F[Boolean] =
          data.modify(m => (m.removed(id), m.contains(id)))

        def upsert(contact: Contact): F[UpdateResult] =
          val id =
            if (contact.id > 0) contact.id.pure[F]
            else ids.updateAndGet(_ + 1)
          id
            .map(id => contact.copy(id = id))
            .flatMap(c =>
              data.modify { m =>
                if (c.email.isDefined && m.values.exists(_.email == c.email))
                  (m, UpdateResult.EmailDuplicate)
                else (m.updated(c.id, c), UpdateResult.Success(c.id))
              }
            )

        def findByEmail(email: Email): F[Option[Contact]] =
          data.get.map(_.values.find(_.email.exists(_ == email)))

        def findById(id: Long): F[Option[Contact]] =
          data.get.map(_.get(id))
      }
    }
