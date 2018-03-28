lazy val `protobuf-to-kafka` = (project in file("."))
  .settings(Seq(
    name := "protobuf-to-kafka",
    version := "0.1",
    scalaVersion := "2.12.5",
    cancelable in Global := true,
    parallelExecution in Test := false,
    fork in Test := true,
    fork in Compile := true
  ))
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ))
.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.16"
  )
)