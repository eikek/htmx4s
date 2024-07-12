package htmx4s.example

import scala.concurrent.duration.*

import cats.effect.*

import htmx4s.example.lib.ContactDb
import htmx4s.http4s.WebjarRoute

import com.comcast.ip4s.*
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger as Http4sLogger

object Main extends IOApp:
  // refers to our own js and css stuff, version is not needed
  private val selfWebjar = WebjarRoute.Webjar("self")("htmx4s-example", "", "")
  def createRoutes(db: ContactDb[IO]): HttpRoutes[IO] = Router.of(
    "/assets" -> WebjarRoute.withHtmx[IO](selfWebjar).serve,
    "/ui" -> contacts.Routes[IO](contacts.RoutesApi(db)).routes
  )

  def run(args: List[String]): IO[ExitCode] =
    ContactDb[IO].use { db =>
      val routes = createRoutes(db)
      EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8888")
        .withHttpApp(
          Http4sLogger.httpApp(true, true)(routes.orNotFound)
        )
        .withShutdownTimeout(0.millis)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }
