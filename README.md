# sbt-update-lines

SBT plugin for updating lines in README and other files as part of a release

## Installing

In `project/plugins.sbt`:

```sbt
addSbtPlugin("co.vitaler" % "sbt-update-line" % "0.0.1") // Latest release
```

## Usage

First, define the `updateLinesSchema` setting, which is a `Seq[UpdateLine]`

```sbt
updateLinesSchema := Seq(
  UpdateLine(
    file("README.md"),
    "// Latest release",
    v => s"""libraryDependencies += "org.example" % "package" % "$v" // Latest release"""
  ),
  UpdateLine(
    file("CHANGELOG.md"),
    "## [Unreleased]",
    v => s"## [Unreleased]\n\n## [$v] - ${java.time.LocalDate.now}"
  )
)
```

The definition of an `UpdateLine` is:

```scala
case class UpdateLine(fileToModify: File, lineMarker: String, replacement: String => String)
```

The `replacement` function receives the version being released, and should return
the replacement for the line where `lineMarker` was found.

Finally, add the `updateLines` release step to your `sbt-release` release process:

```sbt
releaseProcess := Seq(
  // ...
  setReleaseVersion,
  updateLines,
  commitReleaseVersion,
  // ...
)
```

Note that the changes to the files you specify will be committed along with the
changes to `version.sbt` if you call `commitReleaseVersion` (this is usually
what you want).
