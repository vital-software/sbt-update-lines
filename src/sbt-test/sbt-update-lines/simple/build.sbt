import sbtrelease.ReleaseStateTransformations._

import scala.io.{ Codec, Source }

version := "1.0.0-SNAPSHOT"

val unreleasedCompare = """^\[Unreleased\]: https://github\.com/(.*)/compare/(.*)\.\.\.HEAD$""".r

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
  ),
  UpdateLine(
    file("CHANGELOG.md"),
    unreleasedCompare.unapplySeq(_).isDefined,
    (v, compareLine) => compareLine match {
      case unreleasedCompare(project: String, previous: String) =>
        s"[Unreleased]: https://github.com/$project/compare/v$v...HEAD\n[$v]: https://github.com/$project/compare/$previous...v$v"
    }
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
  if (!lines.exists(_.matches("""^\[1\.0\.0\]: https://github.com/example/project/compare/v0\.1\.0\.\.\.v1\.0\.0$"""))) {
    sys.error("Could not find expected update to CHANGELOG.md")
  }
  if (!lines.exists(_.matches("""^\[Unreleased]: https://github.com/example/project/compare/v1\.0\.0\.\.\.HEAD$"""))) {
    sys.error("Could not find expected update to CHANGELOG.md")
  }
  ()
}
