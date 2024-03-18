package htmx4s.example

object Model:

  final case class Name(first: String, last: String):
    val fullName = s"$first $last"
    def contains(s: String): Boolean =
      fullName.toLowerCase.contains(s)

  object Name:
    given Ordering[Name] =
      Ordering.fromLessThan((a, b) => a.fullName < b.fullName)

  final case class Contact(
      id: Long,
      name: Name,
      phone: Option[String],
      email: Option[String]
  ):
    val fullName = name.fullName
    def contains(s: String): Boolean =
      name.contains(s) ||
        email.exists(_.toLowerCase.contains(s)) ||
        phone.exists(_.toLowerCase.contains(s))

  object Contact:
    def withoutId(name: Name, phone: Option[String], email: Option[String]): Contact =
      Contact(-1L, name, phone, email)
