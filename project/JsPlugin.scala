import sbt._
import sys.process._

object JsPlugin extends AutoPlugin {

  object autoImport {
    val jsSourceDirectory = settingKey[File]("The directory containing js sources")
    val jsTargetDirectory = settingKey[File]("The directory to copy js files into")
    val jsEnableTerser = settingKey[Boolean]("Whether to minify js files using terser")
    val jsCopyFiles = taskKey[Seq[File]]("Copy source files to target")
    val jsResources = taskKey[Seq[File]]("Build js files")
  }

  import autoImport._

  override def projectSettings = Seq(
    jsSourceDirectory := (Compile / Keys.sourceDirectory).value / "js",
    jsTargetDirectory := (Compile / Keys.resourceManaged).value / "META-INF" / "resources" / "webjars" / Keys.name.value,
    jsEnableTerser := true,
    jsCopyFiles := {
      val logger = Keys.streams.value.log
      val source = jsSourceDirectory.value
      val target = jsTargetDirectory.value

      logger.info("Copying js files")
      IO.createDirectory(target)
      val files = (source ** "*.js").get.pair(Path.rebase(source, target))
      IO.copy(files).toSeq
    },
    jsResources := {
      val logger = Keys.streams.value.log
      val target = jsTargetDirectory.value
      val files = jsCopyFiles.value
      val isMinify = jsEnableTerser.value
      if (isMinify) {
        logger.info(s"Run minify on ${files.size} js files")
        val allMinJs = target / "all.min.js"
        (Seq(
          "terser",
          "--compress",
          "--mangle",
          "-o",
          allMinJs.getAbsolutePath(),
          "--"
        ) ++ files.map(_.getAbsolutePath())).!
        files :+ allMinJs
      } else files
    },
    Compile / Keys.resourceGenerators += jsResources.taskValue
  )
}
