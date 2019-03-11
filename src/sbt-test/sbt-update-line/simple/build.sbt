import sbtrelease.ReleaseStateTransformations._

import scala.io.{ Codec, Source }

version := "1.0.0-SNAPSHOT"

updateLinesSchema := Seq(
  UpdateLine(
    file("README.md"),
    _.contains("// Latest release"),
    (v, _) => s"""libraryDependencies += "org.example" % "package" % "$v" // Latest release"""
  ),
  UpdateLine(
    file("CHANGELOG.md"),
    _.matches("## \\[Unreleased\\]"),
    (v, _) => s"## [Unreleased]\n\n## [$v] - ${java.time.LocalDate.now}"
  )
)

releaseProcess := Seq(
  inquireVersions,
  runClean,
  setReleaseVersion,
  updateLines,
)

// Test assertions

TaskKey[Unit]("checkReadme") := {
  val lines = Source.fromFile(file("README.md"))(Codec.UTF8).getLines().toList
  if (!lines.contains("libraryDependencies += \"org.example\" % \"package\" % \"1.0.0\" // Latest release")) {
    sys.error("Could not find expected update to README.md")
  }
  ()
}

TaskKey[Unit]("checkChangelog") := {
  val lines = Source.fromFile(file("CHANGELOG.md"))(Codec.UTF8).getLines().toList
  if (!lines.exists(_.matches("""^## \[1\.0\.0\] - .*"""))) {
    sys.error("Could not find expected update to CHANGELOG.md")
  }
  ()
}
