package htmx4s.example

import cats.effect.*
import htmx4s.http4s.WebjarRoute
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.*
import org.http4s.HttpRoutes

object Main extends IOApp:
  val routes: HttpRoutes[IO] = Router.of(
    "/assets" -> WebjarRoute[IO](Seq(WebjarRoute.Webjar.htmx1911)).serve
  )

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .useForever
