package htmx4s.example.lib

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all.*

object Model:
  final case class Name private (first: String, last: String):
    val fullName = s"$first $last"
    def contains(s: String): Boolean =
      fullName.toLowerCase.contains(s)

  object Name:
    given Ordering[Name] =
      Ordering.fromLessThan((a, b) => a.fullName < b.fullName)

    def create(first: String, last: String): ValidatedNel[String, Name] =
      val fname = Validated.condNel(first.nonEmpty, first, "First Name must not be empty")
      val lname = Validated.condNel(last.nonEmpty, last, "Last Name must not be empty")
      (fname, lname).mapN(Name.apply)


  opaque type Email = String
  object Email:
    def apply(email: String): ValidatedNel[String, Email] =
      Validated.condNel(email.contains("@"), email.trim, s"Invalid email: $email")
    extension (self: Email) def value: String = self

  opaque type PhoneNumber = String
  object PhoneNumber:
    def apply(num: String): ValidatedNel[String, PhoneNumber] =
      Validated.condNel(num.nonEmpty, num, s"Invalid phone number: $num")

    extension (self: PhoneNumber) def value: String = self

  final case class Contact(
      id: Long,
      name: Name,
      phone: Option[PhoneNumber],
      email: Option[Email]
  ):
    val fullName = name.fullName
    def contains(s: String): Boolean =
      name.contains(s) ||
        email.exists(_.toLowerCase.contains(s)) ||
        phone.exists(_.toLowerCase.contains(s))

  object Contact:
    def withoutId(name: Name, phone: Option[PhoneNumber], email: Option[Email]): Contact =
      Contact(-1L, name, phone, email)
