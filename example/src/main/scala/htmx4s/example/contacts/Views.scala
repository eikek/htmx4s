package htmx4s.example.contacts

import cats.syntax.all.*

import htmx4s.example.contacts.Model.*
import htmx4s.example.lib.Model.*
import htmx4s.scalatags.Bundle.*

import scalatags.Text.TypedTag
import scalatags.Text.all.doctype

object Views:
  lazy val searchControls: Set[String] = Set(Id.searchBtn, Id.searchInput)
  private object Id {
    val searchBtn = "search-btn"
    val searchInput = "search-input"
  }
  private object Style {
    val link = "text-blue-500 hover:text-blue-600 cursor-pointer"
    val btn =
      "px-2 py-1 rounded border border-blue-500 bg-blue-200 bg-opacity-50 text-blue-800 cursor-pointer hover:bg-opacity-75 dark:border-orange-700 dark:bg-orange-800 dark:text-white"
    val input =
      "text-lg border rounded ml-2 my-1 dark:border-slate-700 border-grey-400 dark:bg-slate-700 dark:text-slate-200  px-1"
    val searchInput =
      "border rounded my-1 dark:border-slate-700 border-grey-400 dark:bg-slate-700 dark:text-slate-200  px-1"
  }

  def layout(titleStr: String)(content: TypedTag[String]) =
    doctype("html")(
      html(
        cls := "dark",
        head(
          title(attr.name := s"Contact- $titleStr"),
          meta(attr.charset := "UTF-8"),
          meta(attr.name := "mobile-web-app-capable", attr.content := "yes"),
          meta(
            attr.name := "viewport",
            attr.content := "width=device-width, initial-scale=1, user-scalable=yes"
          ),
          script(attr.src := "/assets/htmx/htmx.min.js", attr.crossorigin := "anonymous"),
          link(attr.href := "/assets/self/index.css", attr.rel := "stylesheet")
        ),
        body(
          cls := "container mx-auto mx-2 bg-white text-gray-900 dark:bg-slate-800 dark:text-slate-100",
          attr.hxBoost := true,
          h1(cls := "text-3xl font-bold my-4", "Htmx+Scala Contact App"),
          content
        )
      )
    )

  def notFound =
    div(
      h2(cls := "text-2xl font-semibold my-2", "Resource not found!"),
      p("Sorry, this doesn't exist."),
      p(
        a(cls := Style.link, attr.href := "/ui/contacts", "Home")
      )
    )

  def notFoundPage = layout("Not Found")(notFound)

  def showContact(c: Contact) =
    div(
      h2(cls := "text-lg font-bold underline", c.fullName),
      div(
        div("Phone:", c.phone.map(_.value).getOrElse("-")),
        div("Email:", c.email.map(_.value).getOrElse("-"))
      ),
      div(
        cls := "flex flex-row items-center space-x-2 mt-4",
        a(cls := Style.btn, attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
        a(cls := Style.btn, attr.href := "/ui/contacts", "Back")
      )
    )

  def showContactPage(m: ContactShowPage) =
    layout(m.contact.fullName)(showContact(m.contact))

  def errorList(errors: List[String]): TypedTag[String] =
    val hidden = if (errors.isEmpty) "hidden" else ""
    ul(attr.`class` := s"text-red-500 $hidden", errors.map(m => li(m)))

  def errorList(
      errors: Option[Errors],
      key: ContactError.Key
  ): TypedTag[String] =
    errorList(errors.flatMap(_.find(key)).map(_.toList).getOrElse(Nil))

  def editContact(
      c: ContactEditForm,
      id: Option[Long],
      formErrors: Option[Errors]
  ) =
    div(
      form(
        attr.action := id.fold("/ui/contacts/new")(n => s"/ui/contacts/$n/edit"),
        attr.method := "POST",
        fieldset(
          cls := "flex flex-col border border-gray-100 dark:border-slate-700 px-4 py-2",
          legend(cls := "font-semibold px-2", "Contact Values"),
          div(
            cls := "flex flex-col md:w-1/2",
            label(attr.`for` := "email", cls := "font-semibold text-md", "Email"),
            id.map(n =>
              input(
                cls := "hidden",
                attr.`type` := "hidden",
                attr.name := "id",
                attr.value := n
              )
            ),
            input(
              cls := Style.input,
              attr.name := "email",
              attr.id := "email",
              attr.`type` := "email",
              attr.placeholder := "Email",
              attr.value := c.email.orEmpty,
              attr.hxGet := "/ui/contacts/email-check",
              attr.hxInclude := "[name='id']",
              attr.hxTarget := "next .error-list",
              attr.hxSwap := "outerHTML",
              attr.hxTrigger := "change, keyup delay:200ms changed"
            ),
            errorList(formErrors, ContactError.Key.Email)
          ),
          div(
            cls := "flex flex-col md:w-1/2",
            label(
              attr.`for` := "name.first",
              cls := "font-semibold text-md",
              "First Name"
            ),
            input(
              cls := Style.input,
              attr.name := "firstName",
              attr.id := "name.first",
              attr.`type` := "text",
              attr.placeholder := "First Name",
              attr.value := c.firstName
            ),
            errorList(formErrors, ContactError.Key.FirstName)
          ),
          div(
            cls := "flex flex-col md:w-1/2",
            label(attr.`for` := "name.last", cls := "font-semibold text-md", "Last Name"),
            input(
              cls := Style.input,
              attr.name := "lastName",
              attr.id := "name.last",
              attr.`type` := "text",
              attr.placeholder := "Last Name",
              attr.value := c.lastName
            ),
            errorList(formErrors, ContactError.Key.LastName)
          ),
          div(
            cls := "flex flex-col md:w-1/2",
            label(attr.`for` := "phone", cls := "font-semibold text-md", "Phone"),
            input(
              cls := Style.input,
              attr.name := "phone",
              attr.id := "phone",
              attr.`type` := "phone",
              attr.placeholder := "Phone",
              attr.value := c.phone.orEmpty
            ),
            errorList(formErrors, ContactError.Key.Phone)
          ),
          div(
            errorList(formErrors, ContactError.Key.Default)
          ),
          div(
            cls := "mt-4 flex flex-row items-center space-x-2",
            button(cls := Style.btn, "Save"),
            id.map { n =>
              button(
                cls := Style.btn + " mx-3",
                attr.hxDelete := s"/ui/contacts/$n",
                attr.hxTarget := "body",
                attr.hxPushUrl := true,
                attr.hxConfirm := "Are you sure you want to delete this contact?",
                "Delete Contact"
              )
            },
            a(cls := Style.btn, attr.href := "/ui/contacts", "Back")
          )
        )
      )
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
        cls := "flex flex-col",
        div(
          cls := "h-8 flex flex-row items-center my-1",
          div(
            input(
              cls := Style.searchInput + " h-full mr-1",
              attr.id := Id.searchInput,
              attr.`type` := "search",
              attr.name := "q",
              attr.value := m.query.getOrElse(""),
              attr.hxGet := "/ui/contacts",
              attr.hxTarget := "table",
              attr.hxTrigger := "search, keyup delay:200ms changed",
              attr.hxPushUrl := true
            ),
            a(
              cls := Style.btn,
              attr.hxGet := "/ui/contacts",
              attr.hxInclude := "#search",
              attr.hxTarget := "table",
              attr.id := Id.searchBtn,
              attr.hxPushUrl := true,
              "Search"
            )
          ),
          div(
            cls := "flex-grow flex flex-row items-center justify-end",
            a(
              cls := Style.link + " inline-block",
              attr.href := "/ui/contacts/new",
              "Add Contact"
            )
          )
        ),
        div(
          contactTable(m.contacts, m.page)
        )
      )
    )

  def contactTable(contacts: List[Contact], page: Int) =
    table(
      cls := "w-full table-auto border-collapse",
      thead(
        tr(
          cls := "py-4 bg-grey-100 dark:bg-slate-700 border-b border-grey-100 dark:border-slate-700",
          th(cls := "text-center", "Id"),
          th(cls := "text-left", "Name"),
          th(cls := "text-left", "E-Mail"),
          th(cls := "text-left", "Phone"),
          th("")
        )
      ),
      tbody(
        contacts.map(c =>
          tr(
            cls := "py-2 border-b border-grey-100 dark:border-slate-700",
            td(cls := "text-center px-2", c.id),
            td(cls := "text-left px-2", c.fullName),
            td(cls := "text-left px-2", c.email.map(_.value).getOrElse("-")),
            td(cls := "text-left px-2", c.phone.map(_.value).getOrElse("-")),
            td(
              cls := "flex flex-row items-center justify-end space-x-2 text-sm",
              a(cls := Style.btn, attr.href := s"/ui/contacts/${c.id}/edit", "Edit"),
              a(cls := Style.btn, attr.href := s"/ui/contacts/${c.id}", "View")
            )
          )
        ),
        Option(contacts.size).filter(_ >= 10).map { _ =>
          tr(
            cls := "my-1 px-2",
            td(
              attr.colspan := 5,
              button(
                cls := Style.btn,
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
