package observatory.viz

import observatory.util.{ConversionUtil, GeoInterpolationUtil}
import observatory.{Color, Location, Main, Visualization}

/**
	* Created by philba on 4/24/17.
	*/


import java.lang.Math.pow

import observatory.util.Profiler
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.sum
import org.slf4j.LoggerFactory

import scala.collection.mutable

object SparkViz {

	val p = 6

	Main.loggerConfig

	import Main.spark
	import spark.implicits._

	private val logger = LoggerFactory.getLogger(SparkViz.getClass)

	private def approxTemperatureSpark(temperatures: Iterable[(Location, Double)], location: Location) = {
		val result = spark.sparkContext
			.parallelize(temperatures.toSeq)
			.toDF("location", "temperature")
			.map(r => {
				val temp = r.getAs[Double]("temperature")
				val locRow = r.getAs[Row]("location")
				val distance = GeoInterpolationUtil.approximateDistance(locRow.getAs[Double]("lat"),
					locRow.getAs[Double]("lon"), location)
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
			val distance = GeoInterpolationUtil.approximateDistance(r.getAs("latitude"), r.getAs("longitude"), location)
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
				val distance = GeoInterpolationUtil.approximateDistance(locationDatapoint.lat, locationDatapoint.lon, location)
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
				val distance = GeoInterpolationUtil.approximateDistance(locationDatapoint.lat, locationDatapoint.lon, location)
				val invWeight = 1 / pow(distance, p)
				(temp * invWeight, invWeight)
			})
			.cache()

		val result1 = result.aggregate(0.0)(_ + _._1, _ + _)
		val result2 = result.aggregate(0.0)(_ + _._2, _ + _)
		result.unpersist()
		(result1, result2)
	}

	private def computeImgValuesRDD(temperatures: Iterable[(Location, Double)],
																	colors: Iterable[(Double, Color)],
																	xyValues: Seq[(Int, Int)],
																	scale: Int = 1): Seq[((Int, Int), Color)] = {
		Profiler.runProfiled("computeImgValuesRDD") {
			val conversionUtil = new ConversionUtil()
			val cache = mutable.HashMap[Double, Color]()
			val xyColors = spark.sparkContext
				.parallelize(xyValues)
				.map(xy => {
					val temperature = Visualization.predictTemperature(temperatures, conversionUtil.pixelToGps(xy._1, xy._2,
						scale))
					val color = cache.getOrElseUpdate(temperature, Visualization.interpolateColor(colors, temperature))
					(xy, color)
				})
				.collect()
			xyColors
		}
	}

}
