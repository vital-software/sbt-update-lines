addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.2")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.0")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
