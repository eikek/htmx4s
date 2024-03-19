package htmx4s.example.lib

import cats.effect.*
import cats.syntax.all.*
import Model.*

trait ContactDb[F[_]]:
  def search(query: Option[String]): F[List[Contact]]
  def delete(id: Long): F[Unit]
  def upsert(contact: Contact): F[Long]
  def findById(id: Long): F[Option[Contact]]

object ContactDb:
  def apply[F[_]: Sync]: F[ContactDb[F]] =
    val contacts = Ref.of[F, Map[Long, Contact]](Map.empty)
    val idGen = Ref.of[F, Long](0)
    (contacts, idGen).mapN { case (data, ids) =>
      new ContactDb[F] {
        def search(query: Option[String]): F[List[Contact]] =
          val q = query.map(_.toLowerCase)
          data.get.map(_.values.filter(c => q.exists(c.contains)).toList.sortBy(_.name))

        def delete(id: Long): F[Unit] =
          data.update(_.removed(id)).void

        def upsert(contact: Contact): F[Long] =
          val id =
            if (contact.id > 0) contact.id.pure[F]
            else ids.updateAndGet(_ + 1)
          id
            .map(id => contact.copy(id = id))
            .flatTap(c => data.update(_.updated(c.id, c)))
            .map(_.id)

        def findById(id: Long): F[Option[Contact]] =
          data.get.map(_.get(id))
      }
    }
