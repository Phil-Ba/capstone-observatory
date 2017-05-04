package observatory

import java.util.concurrent.TimeUnit

import monix.eval.Task
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.viz.VisualizationGeneric

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
	* 4th milestone: value-added information
	*/
object Manipulation {

	/**
		* @param temperatures Known temperatures
		* @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
		*         returns the predicted temperature at this location
		*/
	def makeGrid(temperatures: Iterable[(Location, Double)]): (Int, Int) => Double = {
		import monix.execution.Scheduler.Implicits.global

		val obsTemperatures = Observable.fromIterable(VisualizationGeneric.mapToOptimizedLocations(temperatures))
		val gridFunction = { (lat: Int, lon: Int) =>
			val tempTask = temperatureAtLocation(obsTemperatures, lat, lon)
			val result = tempTask.runAsync
			Await.result(result, Duration(10, TimeUnit.SECONDS))
		}
		gridFunction
	}

	private def temperatureAtLocation(obsTemperatures: Observable[(OptimizedLocation, Double)],
																		lat: Int, lon: Int): Task[Double] = {
		VisualizationGeneric.approxTemperature(obsTemperatures, Location(lat, lon),
			GeoInterpolationUtil.approximateDistance(_: OptimizedLocation, _))
	}

	/**
		* @param temperaturess Sequence of known temperatures over the years (each element of the collection
		*                      is a collection of pairs of location and temperature)
		* @return A function that, given a latitude and a longitude, returns the average temperature at this location
		*/
	def average(temperaturess: Iterable[Iterable[(Location, Double)]]): (Int, Int) => Double = {
		import monix.execution.Scheduler.Implicits.global

		val avgFunction = { (lat: Int, lon: Int) =>
			val sum = Observable.fromIterable(temperaturess).mapAsync(25)(temp => {
				val obsTemperatures = Observable.fromIterable(VisualizationGeneric.mapToOptimizedLocations(temp))
				temperatureAtLocation(obsTemperatures, lat, lat)
			}).sumL
			Await.result(sum.runAsync, Duration(10, TimeUnit.SECONDS)) / temperaturess.size
		}
		avgFunction
	}

	/**
		* @param temperatures Known temperatures
		* @param normals      A grid containing the “normal” temperatures
		* @return A sequence of grids containing the deviations compared to the normal temperatures
		*/
	def deviation(temperatures: Iterable[(Location, Double)], normals: (Int, Int) => Double): (Int, Int) => Double = {
		import monix.execution.Scheduler.Implicits.global
		???
		val obsTemperatures = Observable.fromIterable(VisualizationGeneric.mapToOptimizedLocations(temperatures))
		val devFunction = { (lat: Int, lon: Int) =>
			val currentTempTask = temperatureAtLocation(obsTemperatures, lat, lon)
			val normalTemp = normals(lat, lon)
			val currentTemp = Await.result(currentTempTask.runAsync, Duration(10, TimeUnit.SECONDS))
			currentTemp - normalTemp
		}
		devFunction
	}


}

