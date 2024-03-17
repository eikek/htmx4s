import sbt._

object HtmxScalatagsGenerator extends AutoPlugin {

  object autoImports {
    type SectionAnchors = Model.SectionAnchors
    val SectionAnchors = Model.SectionAnchors

    val htmxRepositoryUrl = settingKey[String]("The git url to the htmx repository")
    val htmxRepositoryRef = settingKey[String]("The git ref to use")
    val htmxRepositoryTarget =
      settingKey[File]("The target directory for downloading htmx repo")
    val htmxReferencePath = settingKey[String]("The path to the reference.md file")

    val htmxSectionAnchors = settingKey[SectionAnchors](
      "Anchor names in the md file that contain tables of data"
    )

    val htmxDownloadRepository = taskKey[File]("Download htmx repository")
    val htmxParseReferenceDoc =
      taskKey[Model.HtmxConstants]("Parse htmx reference.md file")
    val htmxGenerateSources = taskKey[Seq[File]]("Generate source files")
  }

  import autoImports._

  val htmxSettings = Seq(
    htmxRepositoryUrl := "https://github.com/bigskysoftware/htmx",
    htmxRepositoryRef := "v1.9.11",
    htmxRepositoryTarget := (Compile / Keys.target).value / "htmx-repo",
    htmxReferencePath := "www/content/reference.md",
    htmxSectionAnchors := SectionAnchors(),
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
      val anchors = htmxSectionAnchors.value
      MarkdownParser.readAll(repo, refmd, anchors)
    },
    htmxGenerateSources := {
      val data = htmxParseReferenceDoc.value
      val out = (Compile / Keys.sourceManaged).value / "htmx-constants"
      IO.createDirectory(out)
      val attrFile = Model
        .Source(
          "HtmxAttributes",
          List("scalatags.generic.Attr"),
          data.attributes.attrs
        )
        .writeTo(out, n => s"Attr($n)")
      val clsFile = Model
        .Source(
          "HtmxClasses",
          List(),
          data.classes.cls
        )
        .writeTo(out, identity)
      val reqHeaderFile = Model
        .Source(
          "HtmxRequestHeaders",
          List(),
          data.reqHeader.headers
        )
        .writeTo(out, n => s"HtmxHeader($n)")
      val respHeaderFile = Model
        .Source(
          "HtmxResponseHeaders",
          List(),
          data.respHeader.headers
        )
        .writeTo(out, n => s"HtmxHeader($n)")
      val eventFile = Model
        .Source(
          "HtmxEvents",
          List(),
          data.events.events
        )
        .writeTo(out, identity)

      Seq(attrFile, clsFile, reqHeaderFile, respHeaderFile, eventFile)
    },
    Compile / Keys.sourceGenerators += htmxGenerateSources.taskValue
  )

  override def projectSettings: Seq[Setting[_]] =
    htmxSettings
}

object Model {
  final case class SectionAnchors(
      coreAttrs: String = "#attributes",
      additionalAttrs: String = "#attributes-additional",
      cssClasses: String = "#classes",
      requestHeaders: String = "#request_headers",
      responseHeaders: String = "#response_headers",
      events: String = "#events"
  )

  final case class Source(name: String, imports: List[String], vals: List[Const]) {
    def render(wrap: String => String) = {
      val imp = imports.map(e => s"import $e").mkString("\n")
      val prefix = s"trait $name {"
      val suffix = s"}\nobject $name extends $name"
      val valDefs = vals.map(_.render(wrap)).mkString("\n")
      s"""package htmx4s.scalatags
         |
         |$imp
         |
         |$prefix
         |$valDefs
         |$suffix""".stripMargin
    }

    def writeTo(dir: File, wrap: String => String): File = {
      val file = dir / s"${name}.scala"
      IO.write(file, render(wrap))
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

  final case class Attributes(attrs: List[Const])
  final case class CssClasses(cls: List[Const])
  final case class RequestHeaders(headers: List[Const])
  final case class ResponseHeaders(headers: List[Const])
  final case class Events(events: List[Const])

  final case class HtmxConstants(
      attributes: Attributes,
      classes: CssClasses,
      reqHeader: RequestHeaders,
      respHeader: ResponseHeaders,
      events: Events
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
      anchors: SectionAnchors
  ): Model.HtmxConstants = {
    def read(a: String, loadDocs: Boolean) = readTable(repo, referencePath, a, loadDocs)
    HtmxConstants(
      Attributes(read(anchors.coreAttrs, true) ++ read(anchors.additionalAttrs, true)),
      CssClasses(read(anchors.cssClasses, false)),
      RequestHeaders(read(anchors.requestHeaders, false)),
      ResponseHeaders(read(anchors.responseHeaders, true)),
      Events(read(anchors.events, false))
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
