import java.nio.charset.StandardCharsets
import java.nio.file.Files

import sbt.Keys.name
import sbtrelease.ReleaseStateTransformations._

import scala.io.{ Codec, Source }
import scala.sys.process.ProcessLogger

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    organization := "co.vitaler",
    name := "sbt-update-lines",
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    sbtPlugin := true,
    crossSbtVersions := Vector("1.1.6", "1.2.8"),
    scalaVersion := "2.12.8"
  )

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")

// Release settings
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  updateReleaseFiles,
  commitReleaseVersion,
  releaseStepCommandAndRemaining("+publishSigned"),
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

val updateReleaseFiles = ReleaseStep { state =>
  updateLine(
    state,
    "README.md",
    "// Latest release",
    v => s"""libraryDependencies += "co.vitaler" % "sbt-update-lines" % "$v" // Latest release"""
  )

  updateLine(
    state,
    "CHANGELOG.md",
    "## [Unreleased]",
    v => s"## [Unreleased]\n\n## [$v] - ${java.time.LocalDate.now}"
  )
}

def updateLine(state: State, fileName: String, marker: String, replacement: String => String): State = {
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

  val fileToModify = Project.extract(state).get(baseDirectory.in(ThisBuild)) / fileName
  val lines = Source.fromFile(fileToModify)(Codec.UTF8).getLines().toList
  val lineNumber = lines.indexWhere(_.contains(marker))

  if (lineNumber == -1) {
    throw new RuntimeException(s"Could not find marker '$marker' in file '${fileToModify.getPath}'")
  }

  val content = lines.updated(lineNumber, replacement(version)).mkString("\n") + "\n"

  Files.write(fileToModify.toPath, content.getBytes(StandardCharsets.UTF_8))
  vcs.add(fileToModify.getAbsolutePath) !! logger

  state
}
