package observatory

import com.google.common.cache.{CacheBuilder, CacheLoader}
import monix.eval.Task
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{GeoInterpolationUtil, Profiler}
import observatory.viz.VisualizationGeneric
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

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

		Profiler.runProfiled("makeGrid", Level.DEBUG) {
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
		Profiler.runProfiled("average", Level.DEBUG) {

			val optTemperaturess = temperaturess.map(VisualizationGeneric.mapToOptimizedLocations)
			val avgFunction = { (lat: Int, lon: Int) =>
				val yearlyTemps = optTemperaturess.map(temp => {
					temperatureAtLocation(temp, lat, lon)
				}).sum
				yearlyTemps / temperaturess.size
			}

			val cache = CacheBuilder.newBuilder()
				.build[(Int, Int), java.lang.Double](new CacheLoader[(Int, Int), java.lang.Double] {
				override def load(latLon: (Int, Int)): java.lang.Double = avgFunction(latLon._1, latLon._2)
			})

			(lat: Int, lon: Int) => cache.get((lat, lon))
		}

	}

	/**
		* @param temperatures Known temperatures
		* @param normals      A grid containing the “normal” temperatures
		* @return A sequence of grids containing the deviations compared to the normal temperatures
		*/
	def deviation(temperatures: Iterable[(Location, Double)], normals: (Int, Int) => Double): (Int, Int) => Double = {
		Profiler.runProfiled("deviation", Level.DEBUG) {
			val optTemperatures = VisualizationGeneric.mapToOptimizedLocations(temperatures)
			val devFunction = { (lat: Int, lon: Int) =>
				val currentTemp = temperatureAtLocation(optTemperatures, lat, lon)
				val normalTemp = normals(lat, lon)
				currentTemp - normalTemp
			}
			//			val cache = CacheBuilder.newBuilder()
			//				.softValues()
			//				.build[(Int, Int), Some[Double]](new CacheLoader[(Int, Int), Some[Double]] {
			//				override def load(latLon: (Int, Int)): Some[Double] = Some(devFunction(latLon._1, latLon._2))
			//			})
			//
			//			(lat: Int, lon: Int) => cache.get((lat, lon)).get
			devFunction
		}
	}

}

