import java.nio.charset.StandardCharsets
import java.nio.file.Files

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import fm.sbt.S3URLHandler
import sbt.Keys.name
import sbtrelease.ReleaseStateTransformations._

import scala.io.{ Codec, Source }
import scala.sys.process.ProcessLogger

organization := "co.vitaler"
name := "sbt-update-lines"

sbtPlugin := true
crossSbtVersions := Vector("1.1.6", "1.2.8")
scalaVersion := "2.12.8"

scriptedLaunchOpts += ("-Dplugin.version=" + version.value)
scriptedBufferLog := false

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "4.3.4" % Test,
  "org.specs2" %% "specs2-mock" % "4.3.4" % Test,
)

Test / scalacOptions ++= Seq("-Yrangepos")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)

// PGP settings
pgpPassphrase := Some(Array())
usePgpKeyHex("1bfe664d074b29f8")

// Publishing
resolvers ++= Seq(
  "Vital Snapshots" at "s3://vital-repo.s3-us-west-2.amazonaws.com/maven/snapshots",
  "Vital Releases" at "s3://vital-repo.s3-us-west-2.amazonaws.com/maven/releases",
)

s3CredentialsProvider := { bucket: String =>
  new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("profile vital-master"),
    new ProfileCredentialsProvider("default"),
    S3URLHandler.defaultCredentialsProviderChain(bucket)
  )
}

publishTo := {
  val repo = "s3://vital-repo.s3-us-west-2.amazonaws.com/maven/"
  if (isSnapshot.value)
    Some("Snapshots" at repo + "snapshots")
  else
    Some("Releases" at repo + "releases")
}
publishMavenStyle := false
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

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
