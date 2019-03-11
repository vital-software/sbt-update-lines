addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.2")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.0")
addSbtPlugin("co.vitaler" % "sbt-update-lines" % "0.0.2")

resolvers += Resolver.bintrayRepo("vitaler", "sbt-plugins")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
