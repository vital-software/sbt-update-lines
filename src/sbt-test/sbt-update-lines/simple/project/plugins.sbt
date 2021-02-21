addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.14")

sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("co.vitaler" % "sbt-update-lines" % x)
  case _       => sys.error(
    """|The system property 'plugin.version' is not defined.
       |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
  )
}
