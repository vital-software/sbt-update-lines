import sbtrelease.ReleaseStateTransformations._

organization := "co.vitaler"
name := "sbt-update-lines"
description := "Updates lines in e.g. README.md files when using sbt-release"

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

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")
enablePlugins(SbtPlugin)

// GPG settings
credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "B9513278AF9A10374E07A88FAA24C7523BD70F36",
  "ignored"
)

// Publishing
bintrayRepository := "sbt-plugins"
bintrayOrganization := Some("vitaler")
publishMavenStyle := false
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

// Release settings
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  updateLines,
  commitReleaseVersion,
  publishArtifacts,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

updateLinesSchema := Seq(
  UpdateLine(
    file("README.md"),
    _.matches("addSbtPlugin.*// Latest release"),
    (v, _) => s"""addSbtPlugin("co.vitaler" % "sbt-update-lines" % "$v")    // Latest release"""
  ),
  UpdateLine(
    file("CHANGELOG.md"),
    _.matches("## \\[Unreleased\\]"),
    (v, _) => s"## [Unreleased]\n\n## [$v] - ${java.time.LocalDate.now}"
  )
)
