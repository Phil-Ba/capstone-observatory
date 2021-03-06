name := course.value ++ "-" ++ assignment.value

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
	"-feature",
	"-deprecation",
	"-encoding", "UTF-8",
	"-unchecked",
	"-Xlint",
	"-Yno-adapted-args",
	"-Ywarn-dead-code",
	"-Ywarn-value-discard",
	"-Xfuture",
	"-Xexperimental"
)

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "3.0.1" % "test",
	"com.google.guava" % "guava" % "21.0",
	"com.sksamuel.scrimage" %% "scrimage-core" % "2.1.6", // for visualization
	// You don’t *have to* use Spark, but in case you want to, we have added the dependency
	"org.apache.spark" %% "spark-sql" % "2.1.0",
	// You don’t *have to* use Monix, but in case you want to, we have added the dependency
	"io.monix" %% "monix" % "2.2.4",
	// You don’t *have to* use fs2, but in case you want to, we have added the dependency
	"co.fs2" %% "fs2-io" % "0.9.2",

	"org.scalacheck" %% "scalacheck" % "1.13.1" % Test,
	"junit" % "junit" % "4.10" % Test
)

courseId := "PCO2sYdDEeW0iQ6RUMSWEQ"

assignmentsMap := Map(
	"observatory" -> Assignment(
		packageName = "observatory",
		key = "l1U9JXBMEea_kgqTjVyNvw",
		itemId = "Cr2wv",
		partId = "CWoWG",
		maxScore = 10d,
		styleScoreRatio = 0.2,
		styleSheet = (baseDirectory.value / "scalastyle" / "observatory.xml").getPath,
		options = Map("Xmx" -> "1500m", "grader-memory" -> "2048", "grader-cpu" -> "2")
	)
)

parallelExecution in Test := false // So that tests are executed for each milestone, one after the other
