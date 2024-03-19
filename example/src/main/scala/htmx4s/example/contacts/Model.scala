package htmx4s.example.contacts

import htmx4s.example.lib.Model.*

object Model:

  final case class ContactListPage(
      contacts: List[Contact],
      query: Option[String]
  )

  final case class ContactEditForm(
    firstName: String,
    lastName: String,
    email: String,
    phone: String
  )

  final case class ContactEditPage(
    contact: Option[Contact],
    validationErrors: ContactError.Errors
  )

  final case class ContactShowPage(
      contact: Contact
  )
