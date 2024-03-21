package htmx4s.http4s

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.Sync

import htmx4s.http4s.WebjarRoute.Webjar

import org.http4s.Uri.Path
import org.http4s._

final class WebjarRoute[F[_]: Sync](
    webjars: Seq[Webjar],
    allowedExtensions: Set[String] = WebjarRoute.defaultExtensions
):

  private val prefix = "/META-INF/resources/webjars"
  private val extensions = allowedExtensions ++ allowedExtensions.map(e => s"$e.gz")

  private def resolve(p: Uri.Path): Option[String] =
    if (webjars.isEmpty) Some(p.renderString)
    else
      findWebjar(p.segments).map { case (wj, path) =>
        (wj.basePath ++ path.map(_.decoded())).mkString("/")
      }

  private def findWebjar(
      segments: Vector[Path.Segment]
  ): Option[(Webjar, Vector[Path.Segment])] =
    segments.headOption match
      case Some(h) =>
        val seg = h.decoded()
        webjars.find(_.segment == seg).map(_ -> segments.tail)
      case None => None

  val serve: HttpRoutes[F] =
    Kleisli {
      case req if req.method == Method.GET =>
        val last = req.pathInfo.segments.lastOption.map(_.decoded()).getOrElse("")
        val containsUp = req.pathInfo.segments.exists(_.encoded.contains(".."))
        val allowed = extensions.exists(last.endsWith(_)) && !containsUp
        resolve(req.pathInfo) match
          case Some(p) if allowed =>
            StaticFile.fromResource(
              name = s"$prefix/$p",
              req = Some(req),
              true
            )
          case _ =>
            OptionT.pure(Response.notFound[F])

      case _ =>
        OptionT.none
    }

object WebjarRoute:
  def forHtmx[F[_]: Sync] = WebjarRoute[F](Seq(Webjar.htmx1911))
  def withHtmx[F[_]: Sync](more: Webjar*) = WebjarRoute[F](Webjar.htmx1911 +: more)

  /** Webjar info required to serve its files. The webjar is expected to be available from
    * the current class loader.
    *
    * @param segment
    *   the segment in the uri path to match this webjar
    * @param name
    *   the name (artifactId) of this webjar
    * @param version
    *   the version of the webjar
    * @param path
    *   a path appended to $name/version that the request path will be relativized against
    */
  final case class Webjar(
      segment: String,
      name: String,
      version: String,
      path: List[String]
  ):
    private[http4s] val basePath = (Vector(name, version) ++ path).filter(_.nonEmpty)

  object Webjar:
    def apply(segment: String)(name: String, version: String, path: String*): Webjar =
      Webjar(segment, name, version, path.toList)
    def htmx(version: String): Webjar = Webjar("htmx")("htmx.org", version, "dist")
    val htmx1911 = htmx("1.9.11")

  val defaultExtensions = Set(
    ".js",
    ".css",
    ".html",
    ".json",
    ".jpg",
    ".png",
    ".eot",
    ".woff",
    ".woff2",
    ".svg",
    ".otf",
    ".ttf",
    ".yml",
    ".xml"
  )
