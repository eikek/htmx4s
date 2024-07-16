import sbt._

object HtmxSourceGeneratorPlugin extends AutoPlugin {

  object autoImport {
    type HtmxFileSetting = Model.FileSetting
    val HtmxFileSetting = Model.FileSetting
    type HtmxSettings = Model.HtmxSettings
    val HtmxSettings = Model.HtmxSettings

    val htmxRepositoryUrl = settingKey[String]("The git url to the htmx repository")
    val htmxRepositoryRef = settingKey[String]("The git ref to use")
    val htmxRepositoryTarget =
      settingKey[File]("The target directory for downloading htmx repo")
    val htmxReferencePath = settingKey[String]("The path to the reference.md file")
    val htmxGenerateSettings = settingKey[HtmxSettings](
      "Configuration to control source code generation"
    )

    val htmxDownloadRepository = taskKey[File]("Download htmx repository")
    val htmxParseReferenceDoc =
      taskKey[Model.HtmxConstants]("Parse htmx reference.md file")
    val htmxGenerateSources = taskKey[Seq[File]]("Generate source files")
  }

  import autoImport._

  val htmxSettings = Seq(
    htmxRepositoryUrl := "https://github.com/bigskysoftware/htmx",
    htmxRepositoryTarget := (Compile / Keys.target).value / "htmx-repo",
    htmxReferencePath := "www/content/reference.md",
    htmxGenerateSettings := HtmxSettings(),
    htmxDownloadRepository := {
      val logger = Keys.streams.value.log
      val repo = htmxRepositoryUrl.value
      val refspec = htmxRepositoryRef.value
      val output = htmxRepositoryTarget.value
      RepositorySync.synchronize(logger, repo, Some(refspec), output)
      output
    },
    htmxParseReferenceDoc := {
      val repo = htmxDownloadRepository.value
      val refmd = htmxReferencePath.value
      val settings = htmxGenerateSettings.value
      MarkdownParser.readAll(repo, refmd, settings)
    },
    htmxGenerateSources := {
      val data = htmxParseReferenceDoc.value
      val out = (Compile / Keys.sourceManaged).value / "htmx-constants"
      val settings = htmxGenerateSettings.value
      IO.createDirectory(out)
      val coreAttrFile = settings.coreAttrs
        .whenEnabled(s => Model.Source("HtmxCoreAttributes", data.coreAttributes, s))
        .map(_.writeTo(out))

      val addAttrFile = settings.additionalAttrs
        .whenEnabled(s =>
          Model.Source("HtmxAdditionalAttributes", data.additionalAttributes, s)
        )
        .map(_.writeTo(out))

      val clsFile = settings.cssClasses
        .whenEnabled(s => Model.Source("HtmxClasses", data.classes, s))
        .map(_.writeTo(out))

      val reqHeaderFile = settings.requestHeaders
        .whenEnabled(s => Model.Source("HtmxRequestHeaders", data.reqHeader, s))
        .map(_.writeTo(out))

      val respHeaderFile = settings.responseHeaders
        .whenEnabled(s => Model.Source("HtmxResponseHeaders", data.respHeader, s))
        .map(_.writeTo(out))

      val eventFile = settings.events
        .whenEnabled(s => Model.Source("HtmxEvents", data.events, s))
        .map(_.writeTo(out))

      Seq(
        coreAttrFile,
        addAttrFile,
        clsFile,
        reqHeaderFile,
        respHeaderFile,
        eventFile
      ).flatten
    },
    Compile / Keys.sourceGenerators += htmxGenerateSources.taskValue
  )

  override def projectSettings: Seq[Setting[_]] =
    htmxSettings
}

object Model {
  final case class FileSetting(
      anchor: String,
      typeParams: String = "",
      imports: List[String] = Nil,
      superClasses: List[String] = Nil,
      packageName: String = "htmx",
      nameWrap: String => String = identity,
      createCompanion: Boolean = true,
      enable: Boolean = true
  ) {
    def withImports(refs: List[String]) = copy(imports = refs)
    def withPackage(name: String) = copy(packageName = name)
    def withNameWrap(f: String => String) = copy(nameWrap = f)
    def withTypeParams(p: String) = copy(typeParams = p)
    def withSuperclasses(sc: List[String]) = copy(superClasses = sc)
    def noCompanion = copy(createCompanion = false)
    def disabled = copy(enable = false)
    def enabled = copy(enable = true)
    def whenEnabled[A](f: FileSetting => A): Option[A] =
      if (enable) Some(f(this)) else None
  }
  final case class HtmxSettings(
      coreAttrs: FileSetting = FileSetting("#attributes"),
      additionalAttrs: FileSetting = FileSetting("#attributes-additional"),
      cssClasses: FileSetting = FileSetting("#classes"),
      requestHeaders: FileSetting = FileSetting("#request_headers"),
      responseHeaders: FileSetting = FileSetting("#response_headers"),
      events: FileSetting = FileSetting("#events")
  ) {
    def modifyCoreAttrs(f: FileSetting => FileSetting) = copy(coreAttrs = f(coreAttrs))
    def modifyAdditionalAttrs(f: FileSetting => FileSetting) =
      copy(additionalAttrs = f(additionalAttrs))
    def modifyCssClasses(f: FileSetting => FileSetting) = copy(cssClasses = f(cssClasses))
    def modifyRequestHeaders(f: FileSetting => FileSetting) =
      copy(requestHeaders = f(requestHeaders))
    def modifyResponseHeaders(f: FileSetting => FileSetting) =
      copy(responseHeaders = f(responseHeaders))
    def modifyEvents(f: FileSetting => FileSetting) = copy(events = f(events))
    def modifyAll(f: FileSetting => FileSetting) = modifyCoreAttrs(f)
      .modifyAdditionalAttrs(f)
      .modifyCssClasses(f)
      .modifyRequestHeaders(f)
      .modifyResponseHeaders(f)
      .modifyEvents(f)
  }
  object HtmxSettings {
    def default: HtmxSettings = HtmxSettings()
  }

  final case class Source(name: String, vals: List[Const], settings: FileSetting) {
    def render = {
      val imp = settings.imports.map(e => s"import $e").mkString("\n")
      val superExt =
        if (settings.superClasses.isEmpty) ""
        else settings.superClasses.mkString(" extends ", " with ", "")

      val prefix = s"trait $name${settings.typeParams} $superExt {"
      val suffix =
        if (settings.createCompanion) s"}\nobject $name extends $name"
        else "}"

      val valDefs = vals.map(_.render(settings.nameWrap)).mkString("\n")
      s"""package ${settings.packageName}
         |
         |$imp
         |
         |$prefix
         |$valDefs
         |$suffix""".stripMargin
    }

    def writeTo(dir: File): File = {
      val file = dir / s"${name}.scala"
      IO.write(file, render)
      file
    }
  }

  final case class Const(name: String, doc: String) {
    private def camelCaseName: String =
      name.split("""[\-:]""").toList match {
        case h :: t if h == "HX" => ("hx" :: t.map(_.capitalize)).mkString
        case h :: t =>
          val chars = h.toCharArray
          chars(0) = chars(0).toLower
          (new String(chars) :: t.map(_.capitalize)).mkString
        case Nil => name
      }

    private def nameQuoted: String = s""""$name""""

    private def docString =
      doc.split("\r?\n").toList.mkString("\n  /** ", "\n    * ", "\n   */")

    def render(wrap: String => String) =
      s"""|  $docString
          |  val ${camelCaseName} = ${wrap(nameQuoted)}""".stripMargin
  }

  final case class HtmxConstants(
      coreAttributes: List[Const],
      additionalAttributes: List[Const],
      classes: List[Const],
      reqHeader: List[Const],
      respHeader: List[Const],
      events: List[Const]
  )
}

object MarkdownParser {
  import Model._
  import com.vladsch.flexmark.util.ast.Node
  import com.vladsch.flexmark.html.HtmlRenderer
  import com.vladsch.flexmark.parser.Parser
  import com.vladsch.flexmark.ast._
  import com.vladsch.flexmark.util.data._
  import com.vladsch.flexmark.util.ast._
  import com.vladsch.flexmark.util.format.MarkdownTable
  import com.vladsch.flexmark.util.misc.Extension
  import com.vladsch.flexmark.ext.tables._
  import com.vladsch.flexmark.ext.anchorlink._
  import scala.jdk.CollectionConverters._

  sealed trait DocElement {
    def start: Int
    def fold[A](fa: Header => A, fb: Table => A): A
    def hasAnchor(a: String): Boolean =
      fold(_.text.endsWith(s"{$a}"), _ => false)
  }
  final case class Header(start: Int, text: String) extends DocElement {
    def fold[A](fa: Header => A, fb: Table => A): A = fa(this)
  }
  final case class Table(start: Int, table: MarkdownTable) extends DocElement {
    def fold[A](fa: Header => A, fb: Table => A): A = fb(this)
  }
  final case class DocLink(name: String, ref: Option[String], shortDocs: String)
  object DocLink {
    val zolaLink = """\[`?([a-zA-Z0-9\*:\-]+)`?\]\(@?([0-9a-zA-Z#:/\-.]+)\)""".r
    val codeFence = """`([a-zA-Z0-9\*:\-]+)`""".r
    def from(s: String, shortDocs: String): DocLink = s match {
      case zolaLink(n, r) => DocLink(n, Some(r), shortDocs)
      case codeFence(n)   => DocLink(n, None, shortDocs)
      case _              => DocLink(s, None, shortDocs)
    }
  }

  def readAll(
      repo: File,
      referencePath: String,
      settings: HtmxSettings
  ): Model.HtmxConstants = {
    def read(a: String, loadDocs: Boolean) = readTable(repo, referencePath, a, loadDocs)
    HtmxConstants(
      settings.coreAttrs.whenEnabled(s => read(s.anchor, true)).toList.flatten,
      settings.additionalAttrs.whenEnabled(s => read(s.anchor, true)).toList.flatten,
      settings.cssClasses.whenEnabled(s => read(s.anchor, false)).toList.flatten,
      settings.requestHeaders.whenEnabled(s => read(s.anchor, false)).toList.flatten,
      settings.responseHeaders.whenEnabled(s => read(s.anchor, true)).toList.flatten,
      settings.events.whenEnabled(s => read(s.anchor, false)).toList.flatten
    )
  }

  def readTable(
      repo: File,
      referencePath: String,
      anchor: String,
      loadLinkDoc: Boolean
  ): List[Model.Const] = {
    val docEls = parse(repo / referencePath)
    val table = findNextOf[DocElement](_.hasAnchor(anchor))(docEls) match {
      case Some(t: Table) => t
      case _ => sys.error(s"No table found for anchor $anchor in $referencePath")
    }
    tableRows(table)
      .map { row =>
        val link = row.getCells.get(0).text.toString
        val docs = row.getCells.get(1).text.toString
        DocLink.from(link, docs)
      }
      .filterNot(e => e.name == "hx-on*" || e.shortDocs.startsWith("has been moved"))
      .toList
      .map(e =>
        Model.Const(
          e.name,
          e.ref match {
            case Some(ref) if loadLinkDoc =>
              readMd(repo / referencePath, ref)
            case _ => e.shortDocs
          }
        )
      )
  }

  def readMd(refFile: File, link: String): String =
    scala.io.Source
      .fromFile(refFile.getParentFile() / link)
      .getLines()
      .drop(3)
      .mkString("\n")
      .trim

  def tableRows(t: Table) =
    t.table.getAllContentRows.asScala.drop(1)

  def parse(file: File): List[DocElement] = {
    val options = new MutableDataSet()
    options.set(Parser.EXTENSIONS, List[Extension](TablesExtension.create()).asJava)
    val parser = Parser.builder(options).build()
    val mkdown = scala.io.Source.fromFile(file).mkString
    val doc = parser.parse(mkdown)
    val handler = new Handler
    val tableExtract = new TableExtractingVisitor(DataHolder.NULL)
    val visitor = new NodeVisitor(
      new VisitHandler(classOf[Heading], handler.visit)
    )
    visitor.visit(doc)
    val headers = handler.headers
    val tables = tableExtract
      .getTables(doc)
      .map(t => Table(t.getTableStartOffset, t))
      .toList

    (headers ::: tables).sortBy(_.start)
  }

  def findNextOf[A](p: A => Boolean)(seq: List[A]): Option[A] =
    seq match {
      case h :: t if p(h) => t.headOption
      case h :: t         => findNextOf(p)(t)
      case Nil            => None
    }

  class Handler {
    var headers: List[Header] = Nil
    def visit(cnt: Heading): Unit = {
      val text = cnt.getText
      val start = cnt.getStartOffset
      headers = Header(start, text.toString.trim) :: headers
    }
  }
}

object RepositorySync {
  import com.github.sbt.git._
  import java.io.File
  import org.eclipse.jgit.api.ResetCommand.ResetType

  def synchronize(
      logger: Logger,
      repo: String,
      refspec: Option[String],
      target: File
  ): Unit =
    if (target.exists) updateRepository(logger, target, refspec)
    else cloneRepository(logger, repo, refspec, target)

  def updateRepository(logger: Logger, base: File, refspec: Option[String]) = {
    logger.info(s"Updating repository at $base")
    val git = JGit(base)
    git.porcelain.fetch().call()
    switchBranch(logger, git, refspec)
  }

  def cloneRepository(
      logger: Logger,
      repo: String,
      refspec: Option[String],
      target: File
  ): Unit = {
    logger.info(s"Downloading repository $repo to $target")
    val jgit = JGit.clone(repo, target)
    switchBranch(logger, jgit, refspec)
  }

  def switchBranch(logger: Logger, git: JGit, refspec: Option[String]) =
    refspec match {
      case Some(ref)
          if ref != git.branch && !git.currentTags.contains(ref) && !git.headCommitSha
            .contains(ref) =>
        logger.info(s"Changing to $ref")
        val cmd = git.porcelain.reset()
        cmd.setMode(ResetType.HARD)
        cmd.setRef(ref)
        val res = cmd.call()
        logger.info(s"Repository now on $res")

      case _ => ()
    }
}
