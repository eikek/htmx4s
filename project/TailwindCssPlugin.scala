import sbt._
import sys.process._

object TailwindCssPlugin extends AutoPlugin {

  object autoImport {
    val tailwindCli = settingKey[String]("The tailwindcss cli tool")
    val tailwindSource = settingKey[File]("The input css file")
    val tailwindOutput = settingKey[File]("The output css file")
    val tailwindConfig = settingKey[File]("The path to the tailwindcss config file")
    val tailwindCompile = taskKey[Seq[File]]("Compile styles")
  }
  import autoImport._

  override val projectSettings = Seq(
    tailwindCli := "tailwindcss",
    tailwindSource := (Compile / Keys.sourceDirectory).value / "css" / "index.css",
    tailwindConfig := (Compile / Keys.sourceDirectory).value / "css" / "tailwind.config.js",
    tailwindOutput := (Compile / Keys.resourceManaged).value / "META-INF" / "resources" / "webjars" / Keys.name.value / "index.css",
    tailwindCompile := {
      val logger = Keys.streams.value.log
      val cli = tailwindCli.value
      val source = tailwindSource.value
      val out = tailwindOutput.value
      val cfg = tailwindConfig.value
      logger.info(s"Compiling stylesheets using $cli")
      IO.createDirectory(out.getParentFile())
      Seq(
        cli,
        "-m",
        "-i",
        source.absolutePath,
        "-c",
        cfg.absolutePath,
        "-o",
        out.absolutePath
      ).!
      Seq(out)
    },
    Compile / Keys.resourceGenerators += tailwindCompile.taskValue
  )
}
