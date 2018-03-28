addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.12")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.1")

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.6"
)