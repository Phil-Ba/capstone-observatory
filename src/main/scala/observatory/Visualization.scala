package observatory

import java.util.concurrent.TimeUnit

import com.sksamuel.scrimage.{Image, Pixel, RGBColor}
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{ColorInterpolationUtil, GeoInterpolationUtil, Profiler}
import observatory.viz.VisualizationGeneric
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
	* 2nd milestone: basic visualization
	*/
object Visualization {

	val p = 6
	val baseWidth: Int = 360
	val baseHeight: Int = 180

	Main.loggerConfig
	val log = LoggerFactory.getLogger(this.getClass)


	/**
		* @param temperatures Known temperaturechs: pairs containing a location and the temperature at this location
		* @param location     Location where to predict the temperature
		* @return The predicted temperature at `location`
		*/
	def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {
		import monix.execution.Scheduler.Implicits.global
		val tempObserv = Observable.fromIterable(temperatures)
		//		val optimizedLocations = VisualizationOpti.mapToOptimizedLocations(temperatures)
		Profiler.runProfiled("predictTemperature", Level.DEBUG) {
			val result = temperatures.find(temp => temp._1 == location)
				.map(_._2)
				.getOrElse({
					val temperature = VisualizationGeneric.approxTemperature(tempObserv, location,
						GeoInterpolationUtil.approximateDistance(_: Location, _: Location))
					Await.result(temperature.runAsync, Duration(7, TimeUnit.MINUTES))
				})
			result
		}
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
		log.debug("visualize: temperatues:{}", temperatures)
		val xyValues = for {
			x <- 0 until baseWidth * scale
			y <- 0 until baseHeight * scale
		} yield {
			(x, y)
		}

		val optimizedLocations = VisualizationGeneric.mapToOptimizedLocations(temperatures)
		val xyColors: Seq[((Int, Int), Color)] = VisualizationGeneric.computeImgValues(optimizedLocations, colors,
			xyValues,
			GeoInterpolationUtil.approximateDistance(_: OptimizedLocation, _: Location),
			scale
		)
		val img = Image(baseWidth * scale, baseHeight * scale)

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

