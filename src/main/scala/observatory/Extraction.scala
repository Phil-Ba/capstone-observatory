package observatory

import java.time.LocalDate

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}

/**
	* 1st milestone: data extraction
	*/
object Extraction {

	case class Station(stn: Option[Int], wban: Option[Int], latitude: Option[Double], longitude: Option[Double])

	val stationStruct = StructType(Seq(
		StructField("stn", DataTypes.IntegerType, true),
		StructField("wban", DataTypes.IntegerType, true),
		StructField("latitude", DataTypes.DoubleType, true),
		StructField("longitude", DataTypes.DoubleType, true)
	))

	case class Temperature(stn: Option[Int], wban: Option[Int], month: Option[Int], day: Option[Int], tempF:
	Option[Double])

	val missingTemp = 9999.9

	val spark: SparkSession =
		SparkSession
			.builder()
			.appName("Time Usage")
			.config("spark.master", "local[4]")
			.getOrCreate()

	import spark.implicits._

	/**
		* @param year             Year number
		* @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
		* @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
		* @return A sequence containing triplets (date, location, temperature)
		*/
	def locateTemperatures(year: Int, stationsFile: String,
												 temperaturesFile: String): Iterable[(LocalDate, Location, Double)] = {
		val stationFileLoc = Extraction.getClass.getResource(stationsFile).toExternalForm
		val stationDf = spark.read
			.option("header", false)
			.schema(stationStruct)
			.csv(stationFileLoc).as[Station]
			.filter((station: Station) => station.latitude.isDefined && station.longitude.isDefined)
			.show()
		null
	}

	/**
		* @param records A sequence containing triplets (date, location, temperature)
		* @return A sequence containing, for each location, the average temperature over the year.
		*/
	def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
		???
	}

}
