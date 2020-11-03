addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.1")
addSbtPlugin("co.vitaler" % "sbt-update-lines" % "0.1.5")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
