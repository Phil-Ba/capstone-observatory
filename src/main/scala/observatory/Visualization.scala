package observatory

import com.sksamuel.scrimage.{Image, Pixel, RGBColor}
import observatory.util.{ColorInterpolationUtil, GeoInterpolationUtil, Profiler}
import observatory.viz.VisualizationGeneric
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

/**
	* 2nd milestone: basic visualization
	*/
object Visualization {

	val R = 6372.8 //radius in km
	val p = 6
	val baseWidth: Int = 360
	val baseHeight: Int = 180

	Main.loggerConfig
	private val tempApproxPool = Main.createFjPool(4)
	private val pixelCalcPool = Main.createFjPool(2)

	private val logger = LoggerFactory.getLogger(Visualization.getClass)


	/**
		* @param temperatures Known temperaturechs: pairs containing a location and the temperature at this location
		* @param location     Location where to predict the temperature
		* @return The predicted temperature at `location`
		*/
	def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {
		//		val optimizedLocations = VisualizationOpti.mapToOptimizedLocations(temperatures)
		Profiler.runProfiled("predictTemperature", Level.DEBUG) {
			val result = temperatures.find(temp => temp._1 == location)
				.map(_._2)
				.getOrElse({
					val result: (Double, Double) = VisualizationGeneric.approxTemperature(temperatures, location,
						GeoInterpolationUtil.approximateDistance(_: Location, _))
					//					val result: (Double, Double) = VisualizationOpti.approxTemperature(optimizedLocations, location)
					result._1 / result._2
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
		val xyValues = for {
			x <- 0 until baseWidth * scale
			y <- 0 until baseHeight * scale
		} yield {
			(x, y)
		}

		//		val optimizedLocations = VisualizationOpti.mapToOptimizedLocations(temperatures)
		//		val xyColors: Seq[((Int, Int), Color)] = VisualizationOpti.computeImgValues(optimizedLocations, colors,
		// xyValues, scale)
		val xyColors: Seq[((Int, Int), Color)] = VisualizationGeneric.computeImgValues(temperatures, colors, xyValues,
			GeoInterpolationUtil.approximateDistance(_: Location, _: Location),
			scale
		)
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

}

