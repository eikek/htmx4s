# htmx4s

This is a scala (3) library aiming to make working with htmx more
convenient.

The `example` project builds the contact demo application from the
[htmx book](https://hypermedia.systems/a-web-1-0-application/) using
this library. Please have a look at the code of the `example` for
getting an idea how this library can be used.

It is comprised of the following modules:

## htmx4s-constants

This module has no dependencies and simply provides constants around
htmx vocabulary as scala values. Using it you can use symbols instead
of strings.

The htmx markdown documentation is used to generate code. It also adds
corresponding paragraphs as scala-doc, so the documentation for each
htmx value is readily available in the IDE.

This module contains traits that have all htmx attributes, events,
request/response header names and classes as scala values.

## htmx4s-scalatags

Provides attribute definitions for htmx. Importing the custom `Bundle`
gives access to all the htmx attributes.

The htmx markdown documentation is also used here to generate code.

## htmx4s-http4s

Provides htmx header definitions for http4s, a small dsl extension for
htmx operations and some other small utilities.


# The example contact app

The `example` folder contains a little application (from the [htmx
book](https://hypermedia.systems/a-web-1-0-application/))
demonstrating using this library. It based on the following idea:

- [http4s](https://http4s.org) as the http stack
- Htmx (and potentially more js stuff) is pulled in via
  [webjars](https://webjars.org)
  - A helper `WebjarRoute` provided by `htmx4s-http4s` allows to
    easily serve assets from webjars
- [Scalatags](https://com-lihaoyi.github.io/scalatags/) with
  `htmx4s-scalatags` for creating the html views
- [tailwindcss](https://tailwindcss.com) is used for styles, using
  their provided binary to create the final css file

JS and CSS is build by sbt via the `TailwindCssPlugin` and `JsPlugin`,
respectively (in the `project/` folder). The resulting files will be
copied into the location used by the webjar standard and they can be
served via the `WebjarRoute` as any other webjar.

## Running the example

The `tailwindcss` binary and `terser` is required, as well as
[sbt](https://scala-sbt.org). If you have [nix](https://nixos.org/nix)
installed, you can run `nix develop` to drop into a shell with
everything ready.

Start sbt in the source root and run

```
sbt> example/reStart
```

in sbt shell. Then go to `http://localhost:8888/ui/contacts` to try
out the contact app.
