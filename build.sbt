name := "BraintreeTest"

version := "1.0"

lazy val `braintreetest` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

//scalaVersion := "2.12.2"
scalaVersion := "2.11.11"

//libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )
libraryDependencies ++= Seq(
  ws,
  specs2 % Test ,
  guice,
  "com.typesafe.play"         %  "play_2.11"              % "2.6.10",
  "com.typesafe.play"         %% "play-json"              % "2.6.8",
  "com.braintreepayments.gateway" % "braintree-java"      % "2.72.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

