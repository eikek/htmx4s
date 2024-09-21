import sbt._

object HtmxCurrentVersion extends AutoPlugin {

  override def projectSettings = Seq(
    Compile / Keys.sourceGenerators += Def.task {
      writeFile("htmx4s.constants", (Compile / Keys.sourceManaged).value)
    }.taskValue
  )

  private val version = Dependencies.V.htmx

  def writeFile(pkg: String, out: File): Seq[File] = {
    val target = out / "htmx-constanst" / "HtmxCurrentVersion.scala"
    IO.createDirectory(out.getParentFile)
    val fields = s"""  val value: String = "${version}""""
    val content = s"""package $pkg
                     |object HtmxCurrentVersion {
                     |$fields
                     |}
                     |""".stripMargin
    IO.write(target, content)
    Seq(target)
  }
}
