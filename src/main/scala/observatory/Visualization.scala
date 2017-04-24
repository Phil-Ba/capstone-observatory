package observatory

import java.lang.Math.pow

import com.sksamuel.scrimage.{Image, Pixel, RGBColor}
import observatory.util.{ColorInterpolationUtil, GeoInterpolationUtil, Profiler}
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

import scala.collection.mutable

/**
	* 2nd milestone: basic visualization
	*/
object Visualization {

	val R = 6372.8 //radius in km
	val p = 6
	val baseWidth: Int = 360
	val baseHeight: Int = 180

	Main.loggerConfig
	val fjPool = Main.fjPool

	private val logger = LoggerFactory.getLogger(Visualization.getClass)

	/**
		* @param temperatures Known temperaturechs: pairs containing a location and the temperature at this location
		* @param location     Location where to predict the temperature
		* @return The predicted temperature at `location`
		*/
	def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {
		Profiler.runProfiled("predictTemperature", Level.DEBUG) {
			val result = temperatures.find(temp => temp._1 == location)
				.map(_._2)
				.getOrElse({
					val result: (Double, Double) = approxTemperatureVanilla(temperatures, location)
					result._1 / result._2
				})
			result
		}
	}

	private def approxTemperatureVanilla(temperatures: Iterable[(Location, Double)], location: Location) = {
		val temperaturesPar = temperatures.par
		temperaturesPar.tasksupport = fjPool
		val result = temperaturesPar
			.flatMap((locAndTemp: (Location, Double)) => {
				val locationDatapoint = locAndTemp._1
				val temp = locAndTemp._2
				val distance = GeoInterpolationUtil.approximateDistanceOpti(locationDatapoint.lat, locationDatapoint.lon,
					location)
				//				val distance = approximateDistance(locationDatapoint.lat, locationDatapoint.lon, location)
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

	/**
		* @param points Pairs containing a value and its associated color
		* @param value  The value to interpolate
		* @return The color that corresponds to `value`, according to the color scale defined by `points`
		*/
	def interpolateColor(points: Iterable[(Double, Color)], value: Double): Color = {
		new ColorInterpolationUtil(points.toSeq).interpolate(value)
	}

	/**
		* @param temperatures Known temperatures
		* @param colors       Color scale
		* @return A 360×180 image where each pixel shows the predicted temperature at its location
		*/
	def visualize(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)],
								scale: Int = 1): Image = {
		val xyValues = for {
			x <- 0 until baseWidth * scale
			y <- 0 until baseHeight * scale
		} yield {
			(x, y)
		}

		val xyColors: Seq[((Int, Int), Color)] = computeImgValuesVanilla(temperatures, colors, xyValues, scale)
		val img = Image(baseWidth * scale, baseHeight * scale)

		Profiler.runProfiled("imgCreation") {
			xyColors.foreach(
				xyColor => {
					val xy = xyColor._1
					val color = xyColor._2
					img.setPixel(xy._1, xy._2, Pixel(RGBColor(color.red, color.green, color.blue)))
				}
			)
			img
		}
	}

	private def computeImgValuesVanilla(temperatures: Iterable[(Location, Double)],
																			colors: Iterable[(Double, Color)],
																			xyValues: Seq[(Int, Int)],
																			scale: Int = 1): Seq[((Int, Int), Color)] = {
		Profiler.runProfiled("computeImgValuesVanilla") {
			val cache = mutable.HashMap[Double, Color]()
			val xyPar = xyValues.par
			xyPar.tasksupport = fjPool
			val xyColors = xyPar.map(xy => {
				val temperature = predictTemperature(temperatures, pixelToGps(xy._1, xy._2, scale))
				val color = cache.getOrElseUpdate(temperature, interpolateColor(colors, temperature))
				(xy, color)
			}).seq
			xyColors
		}
	}

	private[observatory] def pixelToGps(x: Int, y: Int, scale: Int = 1) = {
		Location(baseHeight / 2 - (y / 2), (x / 2) - baseWidth / 2)
	}

}

