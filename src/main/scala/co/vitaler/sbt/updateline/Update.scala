package co.vitaler.sbt.updateline

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import co.vitaler.sbt.updateline.UpdateLinePlugin.autoImport.UpdateLine
import sbtrelease.Vcs

import scala.io.{ Codec, Source }
import scala.sys.process.ProcessLogger

object Update {
  def apply(update: UpdateLine, version: String, logger: ProcessLogger, vcs: Option[Vcs]): Unit = {
    val path = update.fileToModify.toPath

    if (!Files.exists(path)) {
      sys.error(s"$path does not exist")
    }

    val lines: List[String] = Source.fromFile(update.fileToModify)(Codec.UTF8).getLines().toList
    val matchingLines: List[Int] = lines.zipWithIndex.collect {
      case (line, number) if update.lineMatcher(line) => number
    }

    if (matchingLines.isEmpty) {
      sys.error(s"Could not find any matching lines to update in file '$path'")
    }

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
}
