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
          script(attr.src := "/js/htmx/htmx.min.js")
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
      h1("Resource not found!"),
      p(
        a(attr.href := "/ui/contacts", "Back")
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
        a(attr.href := "/ui/contacts/${c.id}/edit", "Edit"),
        a(attr.href := "/ui/contacts", "Back")
      )
    )

  def showContactPage(m: ContactShowPage) =
    layout(m.contact.fullName)(showContact(m.contact))

  def editContact(c: Option[Contact]) =
    div(
      form(
        attr.action := c.fold("/ui/contacts/new")(c => s"/ui/contacts/${c.id}/edit"),
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
              attr.value := c.flatMap(_.email.map(_.value)).getOrElse("")
            ),
            span(attr.`class` := "error", "")
          ),
          p(
            label(attr.`for` := "name.first", "First Name"),
            input(
              attr.name := "name.first",
              attr.id := "name.first",
              attr.`type` := "text",
              attr.placeholder := "First Name",
              attr.value := c.map(_.name.first).getOrElse("")
            ),
            span(attr.`class` := "error", "")
          ),
          p(
            label(attr.`for` := "name.last", "Last Name"),
            input(
              attr.name := "name.last",
              attr.id := "name.last",
              attr.`type` := "text",
              attr.placeholder := "Last Name",
              attr.value := c.map(_.name.last).getOrElse("")
            ),
            span(attr.`class` := "error", "")
          ),
          p(
            label(attr.`for` := "phone", "Phone"),
            input(
              attr.name := "phone",
              attr.id := "phone",
              attr.`type` := "phone",
              attr.placeholder := "Phone",
              attr.value := c.flatMap(_.phone.map(_.value)).getOrElse("")
            ),
            span(attr.`class` := "error", "")
          ),
          button("Save")
        )
      ),
      p(a(attr.href := "/ui/contacts", "Back"))
    )

  def editContactPage(m: ContactEditPage) =
    val title = m.contact.map(_.fullName).map(n => s"Edit $n").getOrElse("Create New")
    layout(title)(editContact(m.contact))

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
            td(c.phone.getOrElse("-")),
            td(c.phone.getOrElse("-")),
            td(
              a(attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
              a(attr.href := s"/ui/contacts/${c.id}", "View")
            )
          )
        )
      )
    )
