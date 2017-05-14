package observatory

import java.util.concurrent.TimeUnit

import com.sksamuel.scrimage.{Image, Pixel, RGBColor}
import monix.eval.Task
import monix.reactive.Observable
import observatory.util.ColorInterpolationUtil
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
	* 5th milestone: value-added information visualization
	*/
object Visualization2 {

	Main.loggerConfig
	private val logger = LoggerFactory.getLogger(this.getClass)

	/**
		* @param x   X coordinate between 0 and 1
		* @param y   Y coordinate between 0 and 1
		* @param d00 Top-left value
		* @param d01 Bottom-left value
		* @param d10 Top-right value
		* @param d11 Bottom-right value
		* @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
		*         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
		*/
	def bilinearInterpolation(
														 x: Double,
														 y: Double,
														 d00: Double,
														 d01: Double,
														 d10: Double,
														 d11: Double
													 ): Double = {
		d00 * (1 - x) * (1 - y) +
			d10 * x * (1 - y) +
			d01 * (1 - x) * y +
			d11 * x * y
	}

	/**
		* @param grid   Grid to visualize
		* @param colors Color scale to use
		* @param zoom   Zoom level of the tile to visualize
		* @param x      X value of the tile to visualize
		* @param y      Y value of the tile to visualize
		* @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
		*/
	def visualizeGrid(
										 grid: (Int, Int) => Double,
										 colors: Iterable[(Double, Color)],
										 zoom: Int,
										 x: Int,
										 y: Int
									 ): Image = {
		val pixelsWithLocation: Seq[(Int, Int, Location)] = Interaction.generatePixelsWithLocations(zoom, x, y)
		val colorUtil = new ColorInterpolationUtil(colors.toSeq)
		val img = Image(256, 256)
		pixelsWithLocation.foreach(
			pixelsWithLocation => {
				val temp = tempForPixel(grid, pixelsWithLocation)
				val color = colorUtil.interpolate(temp)
				img.setPixel(pixelsWithLocation._1, pixelsWithLocation._2,
					Pixel(RGBColor(color.red, color.green, color.blue)))
			}
		)
		img
	}

	def visualizeGridMonix(
													grid: (Int, Int) => Double,
													colors: Iterable[(Double, Color)],
													zoom: Int,
													x: Int,
													y: Int
												): Image = {
		import monix.execution.Scheduler.Implicits.global

		val pixelsWithLocation: Observable[(Int, Int, Location)] = Observable
			.fromIterable(Interaction.generatePixelsWithLocations(zoom, x, y))
		val colorUtil = new ColorInterpolationUtil(colors.toSeq)
		val img = Image(256, 256)

		val total = 256 * 256
		val step = total / 20

		var count = 0
		val async = pixelsWithLocation.mapAsync(25) {
			pixelsWithLocation =>
				Task {
					val temp = tempForPixel(grid, pixelsWithLocation)
					val color = colorUtil.interpolate(temp)
					img.setPixel(pixelsWithLocation._1, pixelsWithLocation._2, Pixel(RGBColor(color.red, color.green, color
						.blue)))
					1
				}
		}.bufferSliding(step, step)
			.foreachL(_ => {
				count += 1
				logger.info("{}% of visualizeGridMonix for {} | ({},{}) done.", (count * 5).asInstanceOf[java.lang.Integer],
					zoom.asInstanceOf[java.lang.Integer], x.asInstanceOf[java.lang.Integer], y.asInstanceOf[java.lang.Integer])
			}
			)

		//			.foreachL((i: Int) => {
		//			count += 1
		//			if (count % step == 0) {
		//				logger.info("{}% of visualizeGridMonix for {} | ({},{}) done.", (count / step).asInstanceOf[java.lang
		// .Integer],
		//					zoom.asInstanceOf[java.lang.Integer], x.asInstanceOf[java.lang.Integer], y.asInstanceOf[java.lang
		// .Integer])
		//			}
		//		})

		Await.ready(async.runAsync, Duration(20, TimeUnit.DAYS))

		img
	}

	private def tempForPixel(grid: (Int, Int) => Double,
													 pixelsWithLocation: (Int, Int, Location)
													): Double = {
		val location = pixelsWithLocation._3
		val yFloor = math.floor(location.lat).toInt
		val xFloor = math.floor(location.lon).toInt
		val d00 = grid(yFloor, xFloor)
		val d01 = grid(yFloor + 1, xFloor)
		val d10 = grid(yFloor, xFloor + 1)
		val d11 = grid(yFloor + 1, xFloor + 1)
		bilinearInterpolation(location.lon - xFloor, location.lat - yFloor, d00, d01, d10, d11)
	}

}
