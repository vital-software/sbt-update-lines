package co.vitaler.sbt.updateline

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import co.vitaler.sbt.updateline.UpdateLinePlugin.autoImport.UpdateLine
import sbt._
import sbtrelease.ReleasePlugin.autoImport.{ ReleaseKeys, releaseVcs }

import scala.io.{ Codec, Source }
import scala.sys.process.ProcessLogger

object Update {
  def apply(state: State, update: UpdateLine): State = apply(state, update.fileToModify, update.lineMarker, update.replacement)

  private def apply(state: State, fileToModify: File, marker: String, replacement: String => String): State = {
    val logger = new ProcessLogger {
      override def err(s: => String): Unit = state.log.info(s)
      override def out(s: => String): Unit = state.log.info(s)
      override def buffer[T](f: => T): T = state.log.buffer(f)
    }

    val vcs = Project.extract(state).get(releaseVcs).getOrElse {
      sys.error("VCS not set")
    }

    val (version: String, _) = state.get(ReleaseKeys.versions).getOrElse {
      sys.error(s"${ReleaseKeys.versions.label} key not set")
    }

    if (!Files.exists(fileToModify.toPath)) {
      sys.error(s"${fileToModify.getPath} does not exist")
    }

    val lines = Source.fromFile(fileToModify.getPath)(Codec.UTF8).getLines().toList
    val lineNumber = lines.indexWhere(_.contains(marker))

    if (lineNumber == -1) {
      throw new RuntimeException(s"Could not find marker '$marker' in file '${fileToModify.getPath}'")
    }

    val content = lines.updated(lineNumber, replacement(version)).mkString("\n") + "\n"

    Files.write(fileToModify.toPath, content.getBytes(StandardCharsets.UTF_8))
    vcs.add(fileToModify.getAbsolutePath) !! logger

    state
  }
}
