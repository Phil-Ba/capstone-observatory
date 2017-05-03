package observatory

import java.util.concurrent.TimeUnit

import com.sksamuel.scrimage.{Image, Pixel, RGBColor}
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{ColorInterpolationUtil, GeoInterpolationUtil, Profiler, SlipperyMap}
import observatory.viz.VisualizationGeneric
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
	* 3rd milestone: interactive visualization
	*/
object Interaction {

	Main.loggerConfig
	private val logger = LoggerFactory.getLogger(this.getClass)

	type PixelWithLocation = (Int, Int, Location)
	type PixelWithColour = (Int, Int, Color)

	/**
		* @param zoom Zoom level
		* @param x    X coordinate
		* @param y    Y coordinate
		* @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap
		*         .org/wiki/Slippy_map_tilenames
		*/
	def tileLocation(zoom: Int, x: Int, y: Int): Location = {
		SlipperyMap.Tile(x, y, zoom).toLocation
	}

	/**
		* @param temperatures Known temperatures
		* @param colors       Color scale
		* @param zoom         Zoom level
		* @param x            X coordinate
		* @param y            Y coordinate
		* @return A 256×256 image showing the contents of the tile defined by `x`, `y` and `zooms`
		*/
	def tile(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)], zoom: Int, x: Int,
					 y: Int): Image = {

		logger.info(s"tile=> x:$x y:$y z:$zoom temps:$temperatures")
		val pixelsWithLocation: Seq[(Int, Int, Location)] = generatePixelsWithLocations(zoom, x, y)

		val optimizedTemperatures = VisualizationGeneric.mapToOptimizedLocations(temperatures)
		val colorUtil = new ColorInterpolationUtil(colors.toSeq)

		val pixels = createPixelsOptimized(Observable.fromIterable(optimizedTemperatures), colorUtil,
			Observable.fromIterable(pixelsWithLocation))
		mapPixels2Image(pixels)
	}

	def generatePixelsWithLocations(zoom: Int, tileX: Int, tileY: Int): Seq[(Int, Int, Location)] = {
		val startX = 256 * tileX
		val startY = 256 * tileY

		val pixelsWithLocation = for {
			x <- 0 until 256
			y <- 0 until 256
		} yield {
			(x, y, tileLocation(zoom + 8, x, y))
			//			(x, y, SlipperyMap.Tile(startX + x, startY + y, zoom).toLocation)
		}
		pixelsWithLocation
	}

	private def createPixelsOptimized(temperatures: Observable[(OptimizedLocation, Double)],
																		colorUtil: ColorInterpolationUtil,
																		pixelsWithLocation: Observable[PixelWithLocation]): Seq[PixelWithColour] = {
		import monix.execution.Scheduler.Implicits.global
		Profiler.runProfiled("createPixels") {
			val pixelsWithColors: Observable[PixelWithColour] = pixelsWithLocation.mapAsync(25)(pixelWithLocation => {
				val temperature = VisualizationGeneric.approxTemperature(temperatures, pixelWithLocation._3,
					GeoInterpolationUtil.approximateDistance(_: OptimizedLocation, _))
				temperature.map(t => (pixelWithLocation._1, pixelWithLocation._2, colorUtil.interpolate(t)))
			})
			Await.result(pixelsWithColors.toListL.runAsync, Duration(7, TimeUnit.MINUTES))
		}
	}

	def mapPixels2Image(pixels: Seq[PixelWithColour]): Image = {
		val img = Image(256, 256)

		pixels.foreach(
			pixelWithColor => {
				val color = pixelWithColor._3
				img.setPixel(pixelWithColor._1, pixelWithColor._2, Pixel(RGBColor(color.red, color.green, color.blue, 127)))
			}
		)
		img
	}

	/**
		* Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
		*
		* @param yearlyData    Sequence of (year, data), where `data` is some data associated with
		*                      `year`. The type of `data` can be anything.
		* @param generateImage Function that generates an image given a year, a zoom level, the x and
		*                      y coordinates of the tile and the data to build the image from
		*/
	def generateTiles[Data](
													 yearlyData: Iterable[(Int, Data)],
													 generateImage: (Int, Int, Int, Int, Data) => Unit,
													 zoomLvl: Int = 3
												 ): Unit = {
		yearlyData.foreach(data => {
			val inputs = generateInputs(zoomLvl, data)
			inputs.foreach(input => {
				val year = input._1
				val zoom = input._2
				val x = input._3
				val y = input._4
				val data = input._5
				Profiler.runProfiled(s"generateTile(x:$x,y:$y,z:$zoom)", Level.DEBUG) {
					generateImage(year, zoom, x, y, data)
				}
			})
		})
	}

	def generateTile[Data](yearlyData: Iterable[(Int, Data)],
												 generateImage: (Int, Int, Int, Int, Data) => Unit,
												 zoomLvl: Int
												): Unit = {
		yearlyData.foreach(data => {
			val inputs = generateInputsForZoomLevel(zoomLvl, data)
			inputs.foreach(input => {
				val year = input._1
				val zoom = input._2
				val x = input._3
				val y = input._4
				val data = input._5
				Profiler.runProfiled(s"generateTile(x:$x,y:$y,z:$zoom)", Level.DEBUG) {
					generateImage(year, zoom, x, y, data)
				}
			})
		})
	}

	def generateInputs[Data](zoomLvl: Int, data: (Int, Data)) = {
		val inputs = for {
			zoom <- 0 to zoomLvl
		} yield {
			generateInputsForZoomLevel(zoom, data)
		}
		inputs.flatten
	}

	def generateInputsForZoomLevel[Data](zoom: Int, data: (Int, Data)) = {
		val tiles = Math.round(Math.pow(2, 2 * zoom) / Math.pow(2, zoom)).toInt
		val inputs = for {
			x <- 0 until tiles
			y <- 0 until tiles
		} yield {
			(data._1, zoom, x, y, data._2)
		}
		inputs
	}
}
