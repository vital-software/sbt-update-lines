addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.17.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
