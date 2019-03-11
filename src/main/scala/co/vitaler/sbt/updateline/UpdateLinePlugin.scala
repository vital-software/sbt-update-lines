package co.vitaler.sbt.updateline

import sbt.{ AutoPlugin, Def, Plugins, _ }
import sbtrelease.{ ReleasePlugin, Vcs }
import sbtrelease.ReleasePlugin.autoImport._

import scala.sys.process.ProcessLogger

object UpdateLinePlugin extends AutoPlugin {
  override val requires: Plugins = ReleasePlugin

  object autoImport {
    /**
     * Represents a line update that should be performed during the updateLines ReleaseStep
     *
     * @param fileToModify The file that should be updated
     * @param lineMatcher A function to match lines that should be updated
     * @param replacement A function that receives the version being released, and the line being updated, and returns the updated line
     * @param updateVcs Whether the updated files should be added to the configured VCS
     */
    case class UpdateLine(fileToModify: File, lineMatcher: String => Boolean, replacement: (String, String) => String, updateVcs: Boolean = true)

    val updateLinesSchema = settingKey[Seq[UpdateLine]]("A sequence of UpdateLine definitions (of lines to update on release)")

    val updateLines = ReleaseStep { state =>
      val logger: ProcessLogger = processLogger(state)
      val version: String = releaseVersion(state)
      val vcs: Option[Vcs] = maybeVcs(state)

      Project.extract(state).get(updateLinesSchema).foreach { update =>
        Update.apply(update, version, logger, vcs)
      }

      state
    }
  }

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    updateLinesSchema := Seq.empty[UpdateLine],
  )

  private def processLogger(state: State): ProcessLogger = new ProcessLogger {
    override def err(s: => String): Unit = state.log.info(s)
    override def out(s: => String): Unit = state.log.info(s)
    override def buffer[T](f: => T): T = state.log.buffer(f)
  }

  private def maybeVcs(state: State): Option[Vcs] =
    Project.extract(state).get(releaseVcs)

  private def releaseVersion(state: State): String = state.get(ReleaseKeys.versions).getOrElse {
    sys.error(s"${ReleaseKeys.versions.label} key not set")
  }._1
}
