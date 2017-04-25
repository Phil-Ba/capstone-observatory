package observatory.viz

import java.lang.Math.pow

import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{ColorInterpolationUtil, ConversionUtil, Profiler}
import observatory.{Color, Location, Main}

import scala.collection.mutable

/**
	* 2nd milestone: basic visualization
	*/
object VisualizationGeneric {

	val R = 6372.8 //radius in km
	val p = 6
	val baseWidth: Int = 360
	val baseHeight: Int = 180

	Main.loggerConfig
	private val tempApproxPool = Main.createFjPool(4)
	private val pixelCalcPool = Main.createFjPool(2)

	def computeImgValues[T](temperatures: Iterable[(T, Double)],
													colors: Iterable[(Double, Color)],
													xyValues: Seq[(Int, Int)],
													distanceFunction: (T, Location) => Double,
													scale: Int = 1): Seq[((Int, Int), Color)] = {
		Profiler.runProfiled("computeImgValues") {
			val cache = mutable.HashMap[Double, Color]()
			val xyPar = xyValues.par
			xyPar.tasksupport = pixelCalcPool
			val util = new ColorInterpolationUtil(colors.toSeq)
			val xyColors = xyPar.map(xy => {
				val temperature = predictTemperature(temperatures, ConversionUtil.pixelToGps(xy._1, xy._2, scale),
					distanceFunction)
				val color = cache.getOrElseUpdate(temperature, util.interpolate(temperature))
				(xy, color)
			}).seq
			xyColors
		}
	}

	def mapToOptimizedLocations(temperatures: Iterable[(Location, Double)]): Iterable[(OptimizedLocation, Double)] =
		temperatures.map(temp => (OptimizedLocation(temp._1), temp._2))

	def approxTemperature[T](temperatures: Iterable[(T, Double)],
													 locationToapprox: Location,
													 distanceFunction: (T, Location) => Double) = {
		val temperaturesPar = temperatures.par
		temperaturesPar.tasksupport = tempApproxPool
		val result = temperaturesPar
			.flatMap((locAndTemp: (T, Double)) => {
				val locationDatapoint = locAndTemp._1
				val temp = locAndTemp._2
				val distance = distanceFunction(locationDatapoint, locationToapprox)
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


	def predictTemperature[T](optimizedValues: Iterable[(T, Double)],
														location: Location,
														distanceFunction: (T, Location) => Double): Double = {
		val result: (Double, Double) = approxTemperature(optimizedValues, location, distanceFunction)
		result._1 / result._2
	}

}

