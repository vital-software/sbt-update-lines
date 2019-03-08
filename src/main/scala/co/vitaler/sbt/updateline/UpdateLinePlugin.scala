package co.vitaler.sbt.updateline

import sbt.{ AutoPlugin, Def, Plugins, _ }
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._

object UpdateLinePlugin extends AutoPlugin {
  override val requires: Plugins = ReleasePlugin

  object autoImport {
    case class UpdateLine(fileToModify: File, lineMarker: String, replacement: String => String)

    val updateLinesSchema = settingKey[Seq[UpdateLine]]("Definition of lines to update")
    val updateLines = ReleaseStep { s =>
      Project.extract(s).get(updateLinesSchema).foldLeft(s)(Update.apply)
    }
  }

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    updateLinesSchema := Seq.empty[UpdateLine],
  )
}
