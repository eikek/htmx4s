import Dependencies.V
import com.github.sbt.git.SbtGit.GitKeys._

addCommandAlias("ci", "make; lint; test")
addCommandAlias(
  "lint",
  "scalafmtSbtCheck; scalafmtCheckAll; Compile/scalafix --check; Test/scalafix --check"
)
addCommandAlias("fix", "Compile/scalafix; Test/scalafix; scalafmtSbt; scalafmtAll")

val sharedSettings = Seq(
  organization := "com.github.eikek",
  scalaVersion := V.scala3,
  scalacOptions ++=
    Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-Ykind-projector:underscores",
      "-Werror",
      "-indent",
      "-print-lines",
      "-Wunused:all"
    ),
  Compile / console / scalacOptions := Seq(),
  Test / console / scalacOptions := Seq(),
  licenses := Seq(
    "Apache-2.0" -> url("https://spdx.org/licenses/Apache-2.0.html")
  ),
  homepage := Some(url("https://github.com/eikek/htmx4s")),
  versionScheme := Some("early-semver")
) ++ publishSettings

lazy val publishSettings = Seq(
  developers := List(
    Developer(
      id = "eikek",
      name = "Eike Kettner",
      url = url("https://github.com/eikek"),
      email = ""
    )
  ),
  Test / publishArtifact := false
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

val testSettings = Seq(
  libraryDependencies ++= Dependencies.munit.map(_ % Test),
  testFrameworks += TestFrameworks.MUnit
)

val scalafixSettings = Seq(
  semanticdbEnabled := true, // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision // use Scalafix compatible version
)

val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    gitHeadCommit,
    gitHeadCommitDate,
    gitUncommittedChanges,
    gitDescribedVersion
  ),
  buildInfoOptions ++= Seq(BuildInfoOption.ToMap, BuildInfoOption.BuildTime),
  buildInfoPackage := "keeper"
)

val constants = project
  .in(file("modules/constants"))
  .enablePlugins(HtmxSourceGeneratorPlugin)
  .disablePlugins(RevolverPlugin)
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
  .settings(
    name := "htmx4s-constants",
    description := "Provides htmx constant as scala values",
    htmxRepositoryRef := s"v${V.htmx}",
    htmxGenerateSettings := HtmxSettings.default
      .modifyAll(_.withPackage("htmx4s.constants"))
  )

val scalatags = project
  .in(file("modules/scalatags"))
  .enablePlugins(HtmxSourceGeneratorPlugin)
  .disablePlugins(RevolverPlugin)
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
  .settings(
    name := "htmx4s-scalatags",
    description := "Provides htmx tags to scalatags",
    htmxRepositoryRef := s"v${V.htmx}",
    htmxGenerateSettings := HtmxSettings.default
      .modifyAll(_.disabled)
      .modifyAll(
        _.withPackage("htmx4s.scalatags")
          .withImports(List("scalatags.generic._"))
          .withNameWrap(n => s"attr($n)")
          .withTypeParams("[Builder, Output <: FragT, FragT]")
          .withSuperclasses(List("Util[Builder, Output, FragT]"))
          .noCompanion
      )
      .modifyCoreAttrs(_.enabled)
      .modifyAdditionalAttrs(_.enabled),
    libraryDependencies ++=
      Dependencies.scalatags
  )

val http4s = project
  .in(file("modules/http4s"))
  .disablePlugins(RevolverPlugin)
  .settings(sharedSettings)
  .settings(testSettings)
  .settings(scalafixSettings)
  .settings(
    name := "htmx4s-http4s",
    description := "Convenience utilities to support htmx in http4s",
    libraryDependencies ++=
      Dependencies.http4s
  )
  .dependsOn(constants)

val example = project
  .in(file("example"))
  .enablePlugins(RevolverPlugin)
  .settings(sharedSettings)
  .settings(scalafixSettings)
  .settings(noPublish)
  .settings(
    name := "htmx4s-example",
    description := "Example using http4s with scalatags",
    libraryDependencies ++= Dependencies.htmx ++
      Dependencies.http4sEmber ++
      Dependencies.http4sScalatags ++
      Dependencies.scribe
  )
  .dependsOn(constants, scalatags, http4s)

val root = project
  .in(file("."))
  .disablePlugins(RevolverPlugin)
  .settings(sharedSettings)
  .settings(noPublish)
  .settings(
    name := "htmx4s-root"
  )
  .aggregate(constants, scalatags, http4s, example)
