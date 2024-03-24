import sbt._

object JsPlugin extends AutoPlugin {

  object autoImport {
    val jsSourceDirectory = settingKey[File]("The directory containing js sources")
    val jsTargetDirectory = settingKey[File]("The directory to copy js files into")
    val jsResources = taskKey[Seq[File]]("Build js files")
  }

  import autoImport._

  override def projectSettings = Seq(
    jsSourceDirectory := (Compile / Keys.sourceDirectory).value / "js",
    jsTargetDirectory := (Compile / Keys.resourceManaged).value / "META-INF" / "resources" / "webjars" / Keys.name.value,
    jsResources := {
      val logger = Keys.streams.value.log
      val source = jsSourceDirectory.value
      val target = jsTargetDirectory.value

      logger.info("Copying js files")
      IO.createDirectory(target)
      val files = (source ** "*.js").get.pair(Path.rebase(source, target))
      IO.copy(files).toSeq
    },
    Compile / Keys.resourceGenerators += jsResources.taskValue
  )
}
