package htmx4s.example.contacts

import cats.syntax.all.*
import htmx4s.example.lib.Model.*
import htmx4s.example.contacts.ContactError.*
import org.http4s.FormDataDecoder
import org.http4s.FormDataDecoder.*
import cats.data.Validated
import htmx4s.http4s.util.ValidationErrors
import htmx4s.http4s.util.ValidationDsl.*

object Model:

  final case class ContactListPage(
      contacts: List[Contact],
      query: Option[String],
      page: Int
  )

  final case class ContactEditForm(
      firstName: String,
      lastName: String,
      email: Option[String],
      phone: Option[String]
  ):
    def toContact(id: Long): ContactValid[Contact] =
      val fn = firstName.asNonEmpty(Key.FirstName, "first name is required")
      val ln = lastName.asNonEmpty(Key.LastName, "last name is required")
      val name =
        (fn, ln).mapN((a, b) => Name.create(a, b).keyed(Key.Name)).andThen(identity)
      val em = email.traverse(Email(_).keyed(Key.Email))
      val ph = phone.traverse(PhoneNumber(_).keyed(Key.Phone))
      val vid = id.valid[Key, String]
      (vid, name, ph, em).mapN(Contact.apply)

  object ContactEditForm:
    given FormDataDecoder[ContactEditForm] =
      (
        field[String]("firstName"),
        field[String]("lastName"),
        fieldOptional[String]("email").sanitized,
        fieldOptional[String]("phone").sanitized
      ).mapN(ContactEditForm.apply)

    val empty: ContactEditForm = ContactEditForm("", "", None, None)

    def from(c: Contact): ContactEditForm =
      ContactEditForm(
        c.name.first,
        c.name.last,
        c.email.map(_.value),
        c.phone.map(_.value)
      )

  final case class ContactEditPage(
      id: Option[Long],
      form: ContactEditForm,
      validationErrors: Option[Errors]
  ):
    def fullName: Option[String] =
      for {
        _ <- id
        fn <- Option(form.firstName).filter(_.nonEmpty)
        ln <- Option(form.lastName).filter(_.nonEmpty)
      } yield s"$fn $ln"

  object ContactEditPage:
    val empty: ContactEditPage = ContactEditPage(None, ContactEditForm.empty, None)

  final case class ContactShowPage(
      contact: Contact
  )
