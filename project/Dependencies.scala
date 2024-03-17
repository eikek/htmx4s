import sbt._

object Dependencies {

  object V {
    val scala3 = "3.3.3"
    val http4s = "0.23.26"
    val http4sScalatags = "0.25.2"
    val munit = "0.7.29"
    val munitCatsEffect = "1.0.7"
    val scalatags = "0.12.0"
  }

  val http4s = Seq(
    "org.http4s" %% "http4s-core" % V.http4s,
    "org.http4s" %% "http4s-dsl" % V.http4s
  )

  val scalatags = Seq("com.lihaoyi" %% "scalatags" % V.scalatags)
  val http4sScalatags = Seq("org.http4s" %% "http4s-scalatags" % V.http4sScalatags)

  val munit = Seq(
    "org.scalameta" %% "munit" % V.munit,
    "org.scalameta" %% "munit-scalacheck" % V.munit,
    "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect
  )
}
