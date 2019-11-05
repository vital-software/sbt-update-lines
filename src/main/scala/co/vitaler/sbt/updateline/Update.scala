package co.vitaler.sbt.updateline

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import co.vitaler.sbt.updateline.UpdateLinePlugin.autoImport.UpdateLine
import sbt.util.Logger
import sbtrelease.Vcs

import scala.io.{ Codec, Source }
import scala.sys.process.ProcessLogger
import scala.util.control.NonFatal

object Update {
  def apply(update: UpdateLine, version: String, logger: ProcessLogger, vcs: Option[Vcs], log: Logger): Unit = {
    val path = update.fileToModify.toPath

    if (!Files.exists(path)) {
      sys.error(s"$path does not exist")
    }

    val lines: List[String] = using(Source.fromFile(update.fileToModify)(Codec.UTF8))(_.getLines().toList)
    val matchingLines: List[Int] = lines.zipWithIndex.collect {
      case (line, number) if update.lineMatcher(line) => number
    }

    if (matchingLines.isEmpty) {
      sys.error(s"Could not find any matching lines to update in file '$path'")
    }

    log.info(s"Updating ${matchingLines.size} matching line${if (matchingLines.size > 1) "s" else ""} in ${path.getFileName}")

    val updated: List[String] = matchingLines.foldLeft(lines) { (content, line) =>
      content.updated(line, update.replacement(version, content(line)))
    }

    Files.write(path, (updated.mkString("\n") + "\n").getBytes(StandardCharsets.UTF_8))

    if (update.updateVcs) {
      val v = vcs.getOrElse(sys.error("Could not find VCS in project"))

      val pb = v.add(update.fileToModify.getAbsolutePath)
      pb !! logger
    }
  }

  /**
   * Allows try-with-resources style usage of a closeable resource
   *
   * In Java, the try-with-resources statement allows AutoCloseable resources to be closed when they go out of scope.
   * Scala doesn't offer similar functionality in the standard library, so we define our own. Note that there are some
   * subtleties in the exception handling here (such as what happens when closing the resource
   * also throws exceptions, and how to deal with InterruptedException)
   *
   * MIT licensed - Copyright (c) 2016 Dmitry Komanov
   *
   * @see https://medium.com/@dkomanov/scala-try-with-resources-735baad0fd7d
   */
  def using[A <: AutoCloseable, B](resource: => A)(block: A => B): B = {
    val r: A = resource
    require(r != null, "resource is null")
    var exception: Throwable = null
    try {
      block(r)
    } catch {
      case NonFatal(e) =>
        exception = e
        throw e
    } finally {
      closeWithSuppressed(r, exception)
    }
  }

  /**
   * Closes the resource and attaches any exceptions that happen during the close as suppressed exceptions, so that
   * they don't hide/replace the original exception which caused the close.
   */
  private def closeWithSuppressed[A <: AutoCloseable](resource: A, e: Throwable): Unit =
    if (e != null) {
      try {
        resource.close()
      } catch {
        case NonFatal(suppressed) =>
          e.addSuppressed(suppressed)
      }
    } else {
      resource.close()
    }
}
