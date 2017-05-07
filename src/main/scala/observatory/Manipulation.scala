package observatory

import monix.eval.Task
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{GeoInterpolationUtil, Profiler}
import observatory.viz.VisualizationGeneric
import org.slf4j.LoggerFactory

/**
	* 4th milestone: value-added information
	*/
object Manipulation {

	Main.loggerConfig
	private val logger = LoggerFactory.getLogger(this.getClass)

	/**
		* @param temperatures Known temperatures
		* @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
		*         returns the predicted temperature at this location
		*/
	def makeGrid(temperatures: Iterable[(Location, Double)]): (Int, Int) => Double = {

		Profiler.runProfiled("makeGrid") {
			//			???
			val optTemperatures = VisualizationGeneric.mapToOptimizedLocations(temperatures)
			val gridFunction = { (lat: Int, lon: Int) =>
				temperatureAtLocation(optTemperatures, lat, lon)
			}
			gridFunction
		}
	}

	private def temperatureAtLocation(obsTemperatures: Observable[(OptimizedLocation, Double)],
																		lat: Int, lon: Int): Task[Double] = {
		VisualizationGeneric.approxTemperature(obsTemperatures, Location(lat, lon),
			GeoInterpolationUtil.approximateDistance(_: OptimizedLocation, _))
	}

	private def temperatureAtLocation(obsTemperatures: Iterable[(OptimizedLocation, Double)],
																		lat: Int, lon: Int): Double = {
		VisualizationGeneric.approxTemperature(obsTemperatures, Location(lat, lon),
			GeoInterpolationUtil.approximateDistance(_: OptimizedLocation, _))
	}

	/**
		* @param temperaturess Sequence of known temperatures over the years (each element of the collection
		*                      is a collection of pairs of location and temperature)
		* @return A function that, given a latitude and a longitude, returns the average temperature at this location
		*/
	def average(temperaturess: Iterable[Iterable[(Location, Double)]]): (Int, Int) => Double = {

		Profiler.runProfiled("average") {
			logger.info("avg123:{}", temperaturess)
			//			???
			val avgFunction = { (lat: Int, lon: Int) =>
				logger.info("avg123:{}|{}", lat, lon)
				val sum = temperaturess.map(temp => {
					temperatureAtLocation(VisualizationGeneric.mapToOptimizedLocations(temp), lat, lat) / temperaturess.size
				}).sum
				sum
			}
			avgFunction
		}

	}

	/**
		* @param temperatures Known temperatures
		* @param normals      A grid containing the “normal” temperatures
		* @return A sequence of grids containing the deviations compared to the normal temperatures
		*/
	def deviation(temperatures: Iterable[(Location, Double)], normals: (Int, Int) => Double): (Int, Int) => Double = {
		Profiler.runProfiled("deviation") {
			//			???
			val optTemperatures = VisualizationGeneric.mapToOptimizedLocations(temperatures)
			val devFunction = { (lat: Int, lon: Int) =>
				val currentTemp = temperatureAtLocation(optTemperatures, lat, lon)
				val normalTemp = normals(lat, lon)
				currentTemp - normalTemp
			}
			devFunction
		}
	}

}

