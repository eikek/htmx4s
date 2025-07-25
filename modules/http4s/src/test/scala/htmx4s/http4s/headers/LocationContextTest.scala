package htmx4s.http4s.headers

import org.http4s.implicits.*

import munit.FunSuite

class LocationContextTest extends FunSuite:

  test("render"):
    val ctx = LocationContext(uri"/test", source = Some("#my"))
    val str = ctx.render
    val decoded = LocationContext.parse(str)
    assertEquals(decoded, Right(ctx))

  test("parse requires 'path'"):
    val str = """{"source":"#my"}"""
    assert(LocationContext.parse(str).isLeft)
