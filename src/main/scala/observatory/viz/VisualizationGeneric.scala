package observatory.viz

import java.lang.Math.pow
import java.util.concurrent.TimeUnit

import monix.eval.Task
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{ColorInterpolationUtil, ConversionUtil, Profiler}
import observatory.{Color, Location, Main}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
	* 2nd milestone: basic visualization
	*/
object VisualizationGeneric {

	val R = 6372.8 //radius in km
	val p = 6
	val baseWidth: Int = 360
	val baseHeight: Int = 180

	Main.loggerConfig

	def computeImgValues[T](temperatures: Iterable[(T, Double)],
													colors: Iterable[(Double, Color)],
													xyValues: Seq[(Int, Int)],
													distanceFunction: (T, Location) => Double,
													scale: Int = 1): Seq[((Int, Int), Color)] = {
		import monix.execution.Scheduler.Implicits.global
		Profiler.runProfiled("computeImgValues") {
			val conversionUtil = new ConversionUtil()
			val tempObserv = Observable.fromIterable(temperatures)

			val util = new ColorInterpolationUtil(colors.toSeq)
			val tasks: Observable[((Int, Int), Color)] = Observable.fromIterable(xyValues)
				.mapAsync(5)(xy =>
					approxTemperature(tempObserv, conversionUtil.pixelToGps(xy._1, xy._2, scale),
						distanceFunction)
						.map(temp => {
							(xy, util.interpolate(temp))
						})
				)

			val future = tasks.toListL.runAsync
			Await.result(future, Duration(7, TimeUnit.MINUTES))
		}
	}

	def mapToOptimizedLocations(temperatures: Iterable[(Location, Double)]): Iterable[(OptimizedLocation, Double)] =
		temperatures.map(temp => (OptimizedLocation(temp._1), temp._2))

	def approxTemperature[T](temperatures: Observable[(T, Double)],
													 locationToapprox: Location,
													 distanceFunction: (T, Location) => Double): Task[Double] = {
		val distancesAndTemps = temperatures
			.map((locAndTemp: (T, Double)) => (distanceFunction(locAndTemp._1, locationToapprox), locAndTemp._2))
			.cache

		distancesAndTemps.filter(_._1 <= 1.0)
			.minByL(_._1)
			.flatMap {
				case Some(min) => Task.now(min._2)
				case None => {
					val res = distancesAndTemps
						.map(distanceAndTemp => {
							val locationDatapoint = distanceAndTemp._1
							val temp = distanceAndTemp._2
							val invWeight = 1 / pow(distanceAndTemp._1, p)
							(temp * invWeight, invWeight)
						})

					val aggregation = res.foldLeftL((0.0, 0.0))((t1, t2) => (t1._1 + t2._1, t1._2 + t2._2))
						.map(sums => sums._1 / sums._2)
					aggregation
				}
			}
	}

}