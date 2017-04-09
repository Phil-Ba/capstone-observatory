package observatory

import org.apache.spark.sql.SparkSession

object Main extends App {

	lazy val spark: SparkSession =
		SparkSession
			.builder()
			.appName("Time Usage")
			.config("spark.master", "local")
			.getOrCreate()

}
