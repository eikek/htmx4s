package htmx4s.example.contacts

import cats.syntax.all.*

import htmx4s.example.contacts.Model.*
import htmx4s.example.lib.Model.*
import htmx4s.scalatags.Bundle.*

import scalatags.Text.TypedTag
import scalatags.Text.all.doctype

object Views:
  val linkStyle = "text-blue-500 hover:text-blue-600 cursor-pointer"
  val btnStyle = "px-2 py-1 rounded border border-blue-500 bg-blue-200 bg-opacity-50 text-blue-800 cursor-pointer hover:bg-opacity-75"
  val inputStyle = "border rounded ml-2 my-1 dark:border-slate-700 border-grey-400 dark:bg-slate-700 dark:text-slate-200  px-1"

  def layout(titleStr: String)(content: TypedTag[String]) =
    doctype("html")(
      html(
        cls := "dark",
        head(
          title(attr.name := s"Contact- $titleStr"),
          meta(attr.charset := "UTF-8"),
          meta(attr.name := "mobile-web-app-capable", attr.content := "yes"),
          meta(attr.name := "viewport", attr.content := "width=device-width, initial-scale=1, user-scalable=yes"),
          script(attr.src := "/assets/htmx/htmx.min.js", attr.crossorigin := "anonymous"),
          link(attr.href := "/assets/self/index.css", attr.rel := "stylesheet")
        ),
        body(
          cls := "container mx-auto mx-2 bg-white text-gray-900 dark:bg-slate-800 dark:text-slate-100",
          attr.hxBoost := true,
          h1(cls := "text-3xl font-bold my-4","Htmx+Scala Contact App"),
          content
        )
      )
    )

  def notFound =
    div(
      h2(cls := "text-2xl font-semibold my-2", "Resource not found!"),
      p("Sorry, this doesn't exist."),
      p(
        a(cls := linkStyle, attr.href := "/ui/contacts", "Home")
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
        a(cls := linkStyle, attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
        a(cls := linkStyle, attr.href := "/ui/contacts", "Back")
      )
    )

  def showContactPage(m: ContactShowPage) =
    layout(m.contact.fullName)(showContact(m.contact))

  def errorList(errors: List[String]): TypedTag[String] =
    val hidden = if (errors.isEmpty) "hidden" else ""
    ul(attr.`class` := s"error-list $hidden", errors.map(m => li(m)))

  def errorList(
      errors: Option[ContactError.Errors],
      key: ContactError.Key
  ): TypedTag[String] =
    errorList(errors.flatMap(_.find(key)).map(_.toList).getOrElse(Nil))

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
          div(
            cls := "flex flex-col w-1/2",
            label(attr.`for` := "email", cls := "font-semibold text-md", "Email"),
            input(
              cls := inputStyle,
              attr.name := "email",
              attr.id := "email",
              attr.`type` := "email",
              attr.placeholder := "Email",
              attr.value := c.email.orEmpty,
              attr.hxGet := "/ui/contacts/email-check",
              attr.hxTarget := "next .error-list",
              attr.hxSwap := "outerHTML",
              attr.hxTrigger := "change, keyup delay:200ms changed"
            ),
            errorList(formErrors, ContactError.Key.email)
          ),
          p(
            label(attr.`for` := "name.first", "First Name"),
            input(
              cls := inputStyle,
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
              cls := inputStyle,
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
              cls := inputStyle,
              attr.name := "phone",
              attr.id := "phone",
              attr.`type` := "phone",
              attr.placeholder := "Phone",
              attr.value := c.phone.orEmpty
            ),
            errorList(formErrors, ContactError.Key.phone)
          ),
          button(cls := btnStyle, "Save")
        )
      ),
      errorList(formErrors, ContactError.Key.default),
      id.map { n =>
        button(
          cls := btnStyle + " mx-3",
          attr.hxDelete := s"/ui/contacts/$n",
          attr.hxTarget := "body",
          attr.hxPushUrl := true,
          attr.hxConfirm := "Are you sure you want to delete this contact?",
          "Delete Contact"
        )
      },
      p(cls := "mx-3", a(cls := linkStyle, attr.href := "/ui/contacts", "Back"))
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
          label(cls := "mr-2", attr.`for` := "search", "Search"),
          input(
            cls := inputStyle,
            attr.id := "search",
            attr.`type` := "search",
            attr.name := "q",
            attr.value := m.query.getOrElse("")
          ),
          button(cls := btnStyle, attr.`type` := "submit", "Search")
        ),
        contactTable(m.contacts, m.page),
        p(a(cls := linkStyle, attr.href := "/ui/contacts/new", "Add Contact"))
      )
    )

  def contactTable(contacts: List[Contact], page: Int) =
    table(
      cls := "w-full table-auto",
      thead(
        tr(th("Id"), th("Name"), th("E-Mail"), th("Phone"), th(""))
      ),
      tbody(
        contacts.map(c =>
          tr(
            cls := "my-1 px-2",
            td(c.id),
            td(c.fullName),
            td(c.email.map(_.value).getOrElse("-")),
            td(c.phone.map(_.value).getOrElse("-")),
            td(
              a(cls := linkStyle, attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
              a(cls := linkStyle, attr.href := s"/ui/contacts/${c.id}", "View")
            )
          )
        ),
        Option(contacts.size).filter(_ >= 10).map { _ =>
          tr(
            cls := "my-1 px-2",
            td(
              attr.colspan := 5,
              button(
                cls := btnStyle,
                attr.hxTarget := "closest tr",
                attr.hxSwap := "outerHTML",
                attr.hxSelect := "tbody > tr",
                attr.hxGet := s"/ui/contacts?page=${page + 1}",
                "Load More"
              )
            )
          )
        }
      )
    )
