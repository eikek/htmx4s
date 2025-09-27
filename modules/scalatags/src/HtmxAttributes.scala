package htmx4s.scalatags

trait HtmxAttributes[Builder, Output <: FragT, FragT]
    extends HtmxCoreAttributes[Builder, Output, FragT]
    with HtmxAdditionalAttributes[Builder, Output, FragT]
