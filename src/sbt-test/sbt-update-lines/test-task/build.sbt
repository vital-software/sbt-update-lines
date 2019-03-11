import scala.io.{ Codec, Source }

version := "1.2.3-SNAPSHOT"

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

// Test assertions

TaskKey[Unit]("checkReadme") := {
  val lines = Source.fromFile(file("README.md"))(Codec.UTF8).getLines().toList
  if (!lines.contains("libraryDependencies += \"org.example\" % \"package\" % \"X.Y.Z\" // Latest release")) {
    sys.error("Could not find expected update to README.md")
  }
  ()
}

TaskKey[Unit]("checkChangelog") := {
  val lines = Source.fromFile(file("CHANGELOG.md"))(Codec.UTF8).getLines().toList
  if (!lines.exists(_.matches("""^## \[X\.Y\.Z] - .*"""))) {
    sys.error("Could not find expected update to CHANGELOG.md")
  }
  ()
}
