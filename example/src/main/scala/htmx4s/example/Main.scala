package htmx4s.example

import cats.effect.*
import htmx4s.http4s.WebjarRoute
import htmx4s.example.lib.ContactDb
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.*
import org.http4s.HttpRoutes
import org.http4s.server.middleware.Logger as Http4sLogger

object Main extends IOApp:
  def createRoutes(db: ContactDb[IO]): HttpRoutes[IO] = Router.of(
    "/js" -> WebjarRoute.forHtmx[IO].serve,
    "/ui" -> contacts.Routes[IO](db).routes
  )

  def run(args: List[String]): IO[ExitCode] =
    for {
      db <- ContactDb[IO]
      routes = createRoutes(db)
      _ <-
        EmberServerBuilder
          .default[IO]
          .withHost(host"0.0.0.0")
          .withPort(port"8888")
          .withHttpApp(
            Http4sLogger.httpApp(true, true)(routes.orNotFound)
          )
          .build
          .useForever
    } yield ExitCode.Success
