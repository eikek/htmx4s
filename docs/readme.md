# htmx4s

This is a scala library aiming to make working with htmx more
convenient.

It is comprised of the following modules, all of it is used in the
`example` project.

## htmx-constants

This module has no dependencies and simply provides constants around
htmx vocabulary as scala values. Using it you can use symbols instead
of strings.


## htmx-scalatags

Provides attribute definitions for htmx. Importing the custom bundle
gives access to all the htmx attributes.


## htmx-http4s

Provides htmx header definitions for http4s, a small dsl extension for
htmx operations and some other small utilities.
