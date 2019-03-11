# sbt-update-lines

SBT plugin for updating lines in README and other files as part of a release

## Installing

In `project/plugins.sbt`:

```sbt
resolvers += Resolver.bintrayIvyRepo("vitaler", "sbt-plugins")
addSbtPlugin("co.vitaler" % "sbt-update-lines" % "0.0.3")     // Latest release
```

## Usage

First, define the `updateLinesSchema` setting, which is a `Seq[UpdateLine]`. The
definition of an `UpdateLine` is:

```scala
case class UpdateLine(
  fileToModify: File,
  lineMatcher: String => Boolean,
  replacement: (String, String) => String,
  updateVcs: Boolean = true
)
```

The `lineMatcher` receives each line, and should return `true` for any that
should be updated. Common uses are `_.contains` or `_.matches`. The `replacement`
function receives the version being released and each line
being updated, and should return a string to use as a replacement line (with no
trailing line terminator). An example might look like:

```sbt
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
```

Finally, to actually update the lines, add the `updateLines` release step to your
`sbt-release` release process (anywhere after the `setReleaseVersion` step which
defines the release version):

```sbt
releaseProcess := Seq(
  // ...
  setReleaseVersion,
  updateLines,
  commitReleaseVersion,
  // ...
)
```

If you leave the `updateVcs` parameter of the `UpdateLine` set to `true`, the
updated lines will be committed along with the changes to `version.sbt` when you
call `commitReleaseVersion`.
