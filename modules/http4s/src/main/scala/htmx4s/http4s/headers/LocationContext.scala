package htmx4s.http4s.headers

import org.http4s.Uri

final case class LocationContext(
  path: Uri,
  source: Option[String],
  event: Option[String],
  handler: Option[String],
  target: Option[String],
  swap: Option[String],
  values: Option[String],
  headers: Option[String],
  select: Option[String]
):

  def render: String = ???
