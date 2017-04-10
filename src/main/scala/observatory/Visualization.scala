package observatory

import java.lang.Math.pow

import com.sksamuel.scrimage.Image
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.sum
import org.slf4j.LoggerFactory

import scala.math._

/**
	* 2nd milestone: basic visualization
	*/
object Visualization {

	val R = 6372.8 //radius in km
	val p = 2

	Main.loggerConfig

	import Main.spark
	import spark.implicits._

	private val logger = LoggerFactory.getLogger(Visualization.getClass)

	/**
		* @param temperatures Known temperatures: pairs containing a location and the temperature at this location
		* @param location     Location where to predict the temperature
		* @return The predicted temperature at `location`
		*/
	def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {
		logger.info("Input size: {}", temperatures.seq.size)
		val t1 = System.nanoTime
		val result = temperatures.find(temp => temp._1 == location)
			.map(_._2)
			.getOrElse({
				val result: (Double, Double) = approxTemperatureSparkRDDNoGroup(temperatures, location)
				result._1 / result._2
			})
		val duration = (System.nanoTime - t1) / 1e9d
		logger.info("predictTemperature took {} seconds!", duration)
		result
	}

	private def approxTemperatureSpark(temperatures: Iterable[(Location, Double)], location: Location) = {
		val result = spark.sparkContext
			.parallelize(temperatures.toSeq)
			.toDF("location", "temperature")
			.map(r => {
				val temp = r.getAs[Double]("temperature")
				val locRow = r.getAs[Row]("location")
				val distance = approximateDistance(locRow.getAs[Double]("lat"), locRow.getAs[Double]("lon"),
					location)
				val invWeight = 1 / pow(distance, p)
				(temp * invWeight, invWeight)
			})
			.select(sum($"_1").as[Double], sum($"_2").as[Double])
			.first()

		result
	}

	private def approxTemperatureSparkPremapped(temperatures: Iterable[(Location, Double)], location: Location) = {
		val result = spark.sparkContext
			.parallelize(temperatures.toSeq)
			.map(loc => (loc._1.lat, loc._1.lon, loc._2))
			.toDF("latitude", "longitude", "temperature")
			//			.toDF("location", "temperature")
			.map(r => {
			val temp = r.getAs[Double]("temperature")
			//				val locRow = r.getAs[Row]("location")
			//				val distance = approximateDistance(locRow.getAs[Double]("lat"), locRow.getAs[Double]("lon"),
			//					location)
			val distance = approximateDistance(r.getAs("latitude"), r.getAs("longitude"), location)
			val invWeight = 1 / pow(distance, p)
			(temp * invWeight, invWeight)
		})
			//			.withColumnRenamed("_1", "weightedTemp")
			//			.withColumnRenamed("_2", "weight")
			.select(sum($"_1").as[Double], sum($"_2").as[Double])
			//			.select(sum($"weightedTemp").as("weightedTemp"), sum($"weight").as("weight"))
			//			.select($"weightedTemp".divide($"weight").as[Double])
			.first()
		result
	}

	private def approxTemperatureSparkRDD(temperatures: Iterable[(Location, Double)], location: Location) = {
		val result = spark.sparkContext
			.parallelize(temperatures.toSeq)
			.flatMap((locAndTemp: (Location, Double)) => {
				val locationDatapoint = locAndTemp._1
				val temp = locAndTemp._2
				val distance = approximateDistance(locationDatapoint.lat, locationDatapoint.lon, location)
				val invWeight = 1 / pow(distance, p)
				Seq((1, temp * invWeight), (2, invWeight))
			})
			.groupByKey()
			.aggregateByKey(0.0D)((sum: Double, values: Iterable[Double]) => sum + values.sum, (d1, d2) => d1 + d2)
			.collectAsMap()
		(result(1), result(2))
	}

	private def approxTemperatureSparkRDDNoGroup(temperatures: Iterable[(Location, Double)], location: Location) = {
		val result = spark.sparkContext
			.parallelize(temperatures.toSeq)
			.map((locAndTemp: (Location, Double)) => {
				val locationDatapoint = locAndTemp._1
				val temp = locAndTemp._2
				val distance = approximateDistance(locationDatapoint.lat, locationDatapoint.lon, location)
				val invWeight = 1 / pow(distance, p)
				(temp * invWeight, invWeight)
			})
			.cache()

		val result1 = result.aggregate(0.0)(_ + _._1, _ + _)
		val result2 = result.aggregate(0.0)(_ + _._2, _ + _)
		result.unpersist()
		(result1, result2)
	}

	private def approxTemperatureVanilla(temperatures: Iterable[(Location, Double)], location: Location) = {
		val result = temperatures.toSeq
			.flatMap((locAndTemp: (Location, Double)) => {
				val locationDatapoint = locAndTemp._1
				val temp = locAndTemp._2
				val distance = approximateDistance(locationDatapoint.lat, locationDatapoint.lon, location)
				val invWeight = 1 / pow(distance, p)
				Seq((1, temp * invWeight), (2, invWeight))
			})
			.groupBy(_._1)

		val v1 = result(1).aggregate(0.0D)(
			(sum: Double, values) => sum + values._2,
			(d1, d2) => d1 + d2)
		val v2 = result(2).aggregate(0.0D)(
			(sum: Double, values) => sum + values._2,
			(d1, d2) => d1 + d2)

		(v1, v2)
	}

	protected[observatory] def approximateDistance(lat: Double, lon: Double, location2: Location): Double = {
		val dLat = (lat - location2.lat).toRadians
		val dLon = (lon - location2.lon).toRadians

		val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(lat.toRadians) * cos(location2.lat.toRadians)
		val c = 2 * asin(sqrt(a))
		R * c
	}

	/**
		* @param points Pairs containing a value and its associated color
		* @param value  The value to interpolate
		* @return The color that corresponds to `value`, according to the color scale defined by `points`
		*/
	def interpolateColor(points: Iterable[(Double, Color)], value: Double): Color = {

		???
	}

	/**
		* @param temperatures Known temperatures
		* @param colors       Color scale
		* @return A 360×180 image where each pixel shows the predicted temperature at its location
		*/
	def visualize(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)]): Image = {
		???
	}

}

