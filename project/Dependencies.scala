import sbt._

object Dependencies {

  object V {
    val scala3 = "3.4.3"
    val doobie = "1.0.0-RC5"
    val h2 = "2.3.232"
    val htmx = "2.0.1"
    val http4s = "0.23.28"
    val http4sScalatags = "0.25.2"
    val munit = "1.0.0"
    val munitCatsEffect = "2.0.0"
    val scalatags = "0.13.1"
    val scribe = "3.15.0"
  }

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % V.doobie,
    "org.tpolecat" %% "doobie-hikari" % V.doobie
  )
  val h2 = Seq(
    "com.h2database" % "h2" % V.h2
  )

  val scribe = Seq(
    "com.outr" %% "scribe" % V.scribe,
    "com.outr" %% "scribe-slf4j2" % V.scribe,
    "com.outr" %% "scribe-cats" % V.scribe
  )

  val htmx = Seq(
    "org.webjars.npm" % "htmx.org" % s"${V.htmx}"
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-core" % V.http4s,
    "org.http4s" %% "http4s-dsl" % V.http4s
  )
  val http4sEmber = Seq(
    "org.http4s" %% "http4s-ember-server" % V.http4s
  )

  val scalatags = Seq("com.lihaoyi" %% "scalatags" % V.scalatags)
  val http4sScalatags = Seq("org.http4s" %% "http4s-scalatags" % V.http4sScalatags)

  val munit = Seq(
    "org.scalameta" %% "munit" % V.munit,
    "org.scalameta" %% "munit-scalacheck" % V.munit,
    "org.typelevel" %% "munit-cats-effect" % V.munitCatsEffect
  )
}
