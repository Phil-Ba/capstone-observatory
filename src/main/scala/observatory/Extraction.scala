package observatory

import java.time.LocalDate

import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}

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

	case class Temperature(stn: Option[Int], wban: Option[Int], month: Int, day: Int, tempF: Double)

	val tempStruct = StructType(Seq(
		StructField("stn", DataTypes.IntegerType, true),
		StructField("wban", DataTypes.IntegerType, true),
		StructField("month", DataTypes.IntegerType, false),
		StructField("day", DataTypes.IntegerType, false),
		StructField("tempF", DataTypes.DoubleType, false)
	))


	val missingTemp = 9999.9

	private val spark: SparkSession =
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
		val stations = readStationsData(stationsFile)
		val temperatures = readTemperatureData(temperaturesFile)

		//		implicit val myObjEncoder = org.apache.spark.sql.Encoders.kryo[LocalDate]
		import scala.reflect.ClassTag
		implicit def kryoEncoder[A](implicit ct: ClassTag[A]) =
			org.apache.spark.sql.Encoders.kryo[A](ct)

		stations.show()
		temperatures.show()

		//
		implicit def tuple3[A1, A2, A3](
																		 implicit e1: Encoder[A1],
																		 e2: Encoder[A2],
																		 e3: Encoder[A3]
																	 ): Encoder[(A1, A2, A3)] = Encoders.tuple[A1, A2, A3](e1, e2, e3)

		stations
//			.join(temperatures, stations("stn") === temperatures("stn") && stations("wban") ===
//				temperatures("wban"))
			//			.join(temperatures, Seq("stn", "wban"))
			//			.join(temperatures, Seq("stn"))
						.join(temperatures, Seq("wban"))
			.show()
		//			.map(r => {
		//			val day = r.getAs[Int]("day")
		//			val month = r.getAs[Int]("month")
		//			val date = LocalDate.of(year, month, day)
		//			val tempC = convertFarenheitToCelsius(r.getAs("tempF"))
		//			val location = Location(r.getAs("latitude"), r.getAs("longitude"))
		//			(date, location, tempC)
		//		})
		//			.collect()
		null
	}


	protected[observatory] def readStationsData(stationsFile: String) = {
		val stationFileLoc = Extraction.getClass.getResource(stationsFile).toExternalForm
		spark.read
			.option("header", false)
			.option("mode", "FAILFAST")
			.schema(stationStruct)
			.csv(stationFileLoc).as[Station]
			.filter((station: Station) => station.latitude.isDefined && station.longitude.isDefined)
	}

	protected[observatory] def convertFarenheitToCelsius(f: Double) = (f - 32) * 5 / 9

	protected[observatory] def readTemperatureData(temperaturesFile: String) = {
		val temperaturesFileLoc = Extraction.getClass.getResource(temperaturesFile).toExternalForm
		spark.read
			.option("header", false)
			.option("mode", "FAILFAST")
			.schema(tempStruct)
			.csv(temperaturesFileLoc).as[Temperature]
			.filter((temp: Temperature) ⇒ temp.tempF != missingTemp)
	}

	/**
		* @param records A sequence containing triplets (date, location, temperature)
		* @return A sequence containing, for each location, the average temperature over the year.
		*/
	def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
		???
	}

}
