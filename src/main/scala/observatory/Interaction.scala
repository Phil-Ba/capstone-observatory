package observatory

import com.sksamuel.scrimage.{Image, Pixel, RGBColor}
import observatory.Visualization.{interpolateColor, predictTemperature}
import observatory.util.{Profiler, SlipperyMap}

import scala.collection.mutable

/**
	* 3rd milestone: interactive visualization
	*/
object Interaction {

	val fjPool = Main.fjPool
	Main.loggerConfig
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
		val latLon = SlipperyMap.Tile(x, y, zoom).toLatLon
		Location(latLon.lat, latLon.lon)
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
		val upperLeft = tileLocation(zoom, x, y)
		val lowerRight = tileLocation(zoom, x + 1, y + 1)

		val startX = upperLeft.lon
		val startY = upperLeft.lat
		val stepX = (lowerRight.lon - upperLeft.lon) / 256
		val stepY = (lowerRight.lat - upperLeft.lat) / 256

		val pixelsWithLocation = for {
			x <- 0 until 256
			y <- 0 until 256
		} yield {
			(x, y, Location(startY + y * stepY, startX + x * stepX))
		}

		val pixels = createPixels(temperatures, colors, pixelsWithLocation)
		mapPixels2Image(pixels)
	}

	private def createPixels(temperatures: Iterable[(Location, Double)],
													 colors: Iterable[(Double, Color)],
													 pixelsWithLocation: Seq[PixelWithLocation]) = {
		Profiler.runProfiled("createPixels") {
			val cache = mutable.HashMap[Double, Color]()
			val xyPar = pixelsWithLocation.par
			xyPar.tasksupport = fjPool
			val pixelsWithColor = xyPar.map(pixelWithLocation => {
				val temperature = predictTemperature(temperatures, pixelWithLocation._3)
				val color = cache.getOrElseUpdate(temperature, interpolateColor(colors, temperature))
				(pixelWithLocation._1, pixelWithLocation._2, color)
			}).seq
			pixelsWithColor
		}
	}

	def mapPixels2Image(pixels: Seq[PixelWithColour]): Image = {
		val img = Image(256, 256)

		Profiler.runProfiled("imgCreation") {
			pixels.foreach(
				pixelWithColor => {
					val color = pixelWithColor._3
					img.setPixel(pixelWithColor._1, pixelWithColor._2, Pixel(RGBColor(color.red, color.green, color.blue, 127)))
				}
			)
			img
		}
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
													 generateImage: (Int, Int, Int, Int, Data) => Unit
												 ): Unit = {
		yearlyData.foreach(data => {
			for {
				zoom <- 0 to 3
				tiles <- 1 to Math.pow(2, 2 * zoom)
				x <- 0 until tiles
				y <- 0 until tiles
			} yield {
				generateImage(data._1, zoom, x, y, data._2)
			}
		})
	}

}
