package observatory

import java.lang.Math.pow

import com.sksamuel.scrimage.Image
import org.apache.spark.sql.functions.sum
import org.apache.spark.sql.{Row, SparkSession}

import scala.math._

/**
	* 2nd milestone: basic visualization
	*/
object Visualization {

	val R = 6372.8 //radius in km
	val p = 2
	private val spark: SparkSession =
		SparkSession
			.builder()
			.appName("Time Usage")
			.config("spark.master", "local[4]")
			.getOrCreate()

	import spark.implicits._
	//		import spark.implicits._

	/**
		* @param temperatures Known temperatures: pairs containing a location and the temperature at this location
		* @param location     Location where to predict the temperature
		* @return The predicted temperature at `location`
		*/
	def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {
		temperatures.find(temp => temp._1 == location)
			.map(_._2)
			.getOrElse({
				spark.sparkContext.parallelize(temperatures.toSeq)
					.toDF("location", "temperature")
					.map(r => {
						val temp = r.getAs[Double]("temperature")
						val locRow = r.getAs[Row]("location")
						val distance = approximateDistance(locRow.getAs[Double]("lat"), locRow.getAs[Double]("lon"), location)
						val invWeight = 1 / pow(distance, p)
						(temp * invWeight, invWeight)
					})
					.withColumnRenamed("_1", "weightedTemp")
					.withColumnRenamed("_2", "weight")
					.select(sum($"weightedTemp").as("weightedTemp"), sum($"weight")).as("weight")
					.select($"weightedTemp".divide($"weight").as[Double])
					.first()
			})
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

