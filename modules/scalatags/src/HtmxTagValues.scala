package htmx4s.scalatags

import scalatags.Text
import scalatags.text.Builder

import htmx4s.constants.{SwapStyle, SwapValue}

trait HtmxTagValues:
  self: Text.Aggregate =>

  extension [A](av: AttrValue[A])
    def contramap[B](f: B => A): AttrValue[B] = new AttrValue[B] {
      def apply(b: Builder, a: Attr, s: B): Unit = av.apply(b, a, f(s))
    }

  given AttrValue[SwapStyle] = stringAttr.contramap(_.render)
  given AttrValue[SwapValue] = stringAttr.contramap(_.render)
