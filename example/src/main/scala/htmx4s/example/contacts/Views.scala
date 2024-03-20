package htmx4s.example.contacts

import htmx4s.example.contacts.Model.*
import htmx4s.example.lib.Model.*
import htmx4s.scalatags.Bundle.*

import scalatags.Text.TypedTag
import scalatags.Text.all.doctype

object Views:

  def layout(titleStr: String)(content: TypedTag[String]) =
    doctype("html")(
      html(
        head(
          title(attr.name := s"Contact- $titleStr"),
          script(attr.src := "/js/htmx/htmx.min.js", attr.crossorigin := "anonymous")
        ),
        body(
          attr.hxBoost := true,
          h1("Htmx+Scala Contact App"),
          content
        )
      )
    )

  def notFound =
    div(
      h2("Resource not found!"),
      p("Sorry, this doesn't exist."),
      p(
        a(attr.href := "/ui/contacts", "Home")
      )
    )

  def notFoundPage = layout("Not Found")(notFound)

  def showContact(c: Contact) =
    div(
      h1(c.fullName),
      div(
        div("Phone:", c.phone.map(_.value).getOrElse("-")),
        div("Email:", c.email.map(_.value).getOrElse("-"))
      ),
      p(
        a(attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
        a(attr.href := "/ui/contacts", "Back")
      )
    )

  def showContactPage(m: ContactShowPage) =
    layout(m.contact.fullName)(showContact(m.contact))


  def errorList(errors:Option[ContactError.Errors], key: ContactError.Key) =
    errors.flatMap(_.find(key)).map { errs =>
      ul(attr.`class` := "error-list", errs.toList.map(m => li(m)))
    }


  def editContact(
      c: ContactEditForm,
      id: Option[Long],
      formErrors: Option[ContactError.Errors]
  ) =
    div(
      form(
        attr.action := id.fold("/ui/contacts/new")(n => s"/ui/contacts/$n/edit"),
        attr.method := "POST",
        fieldset(
          legend("Contact Values"),
          p(
            label(attr.`for` := "email", "Email"),
            input(
              attr.name := "email",
              attr.id := "email",
              attr.`type` := "email",
              attr.placeholder := "Email",
              attr.value := c.email
            ),
            errorList(formErrors, ContactError.Key.email)
          ),
          p(
            label(attr.`for` := "name.first", "First Name"),
            input(
              attr.name := "firstName",
              attr.id := "name.first",
              attr.`type` := "text",
              attr.placeholder := "First Name",
              attr.value := c.firstName
            ),
            errorList(formErrors, ContactError.Key.firstName)
          ),
          p(
            label(attr.`for` := "name.last", "Last Name"),
            input(
              attr.name := "lastName",
              attr.id := "name.last",
              attr.`type` := "text",
              attr.placeholder := "Last Name",
              attr.value := c.lastName
            ),
            errorList(formErrors, ContactError.Key.lastName)
          ),
          p(
            label(attr.`for` := "phone", "Phone"),
            input(
              attr.name := "phone",
              attr.id := "phone",
              attr.`type` := "phone",
              attr.placeholder := "Phone",
              attr.value := c.phone
            ),
            errorList(formErrors, ContactError.Key.phone)
          ),
          button("Save")
        )
      ),
      errorList(formErrors, ContactError.Key.default),
      id.map { n =>
        form(
          attr.action := s"/ui/contacts/$n/delete",
          attr.method := "POST",
          button("Delete Contact")
        )
      },
      p(a(attr.href := "/ui/contacts", "Back"))
    )

  def editContactPage(m: ContactEditPage) =
    val title = m.fullName.map(n => s"Edit $n").getOrElse("Create New")
    layout(title)(
      div(
        editContact(m.form, m.id, m.validationErrors),
        m.validationErrors.map(_ => p("There are form errors"))
      )
    )

  def contactListPage(m: ContactListPage): doctype =
    layout("Contact Search")(
      div(
        form(
          attr.action := "/ui/contacts",
          attr.method := "GET",
          label(attr.`for` := "search", "Search Term"),
          input(
            attr.id := "search",
            attr.`type` := "search",
            attr.name := "q",
            attr.value := m.query.getOrElse("")
          ),
          input(attr.`type` := "submit", attr.value := "Search")
        ),
        contactTable(m.contacts),
        p(a(attr.href := "/ui/contacts/new", "Add Contact"))
      )
    )

  def contactTable(contacts: List[Contact]) =
    table(
      thead(
        tr(th("Id"), th("Name"), th("E-Mail"), th("Phone"), th(""))
      ),
      tbody(
        contacts.map(c =>
          tr(
            td(c.id),
            td(c.fullName),
            td(c.email.map(_.value).getOrElse("-")),
            td(c.phone.map(_.value).getOrElse("-")),
            td(
              a(attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
              a(attr.href := s"/ui/contacts/${c.id}", "View")
            )
          )
        )
      )
    )
