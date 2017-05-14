package observatory.util

import com.google.common.cache.{CacheBuilder, CacheLoader}
import observatory.Color
import observatory.util.ColorInterpolationUtil.RgbPoint

/**
	* Created by philba on 4/11/17.
	*/
class ColorInterpolationUtil(datapointsInput: Seq[(Double, Color)]) {

	private val cache = CacheBuilder.newBuilder()
		.maximumSize(1000000)
		.build[java.lang.Double, Color](new CacheLoader[java.lang.Double, Color] {
		override def load(key: java.lang.Double): Color = calculateColor(key)
	})

	private val datapoints = datapointsInput.sortBy(_._1)

	def interpolate(temperature: Double): Color = cache.get(temperature)

	private def calculateColor(temperature: Double) = {
		val startingPoints = findStartingPoints(temperature)
		val lower = startingPoints._1
		val upper = startingPoints._2
		val r = interpolate((lower._1, lower._2.red), (upper._1, upper._2.red), temperature)
		val g = interpolate((lower._1, lower._2.green), (upper._1, upper._2.green), temperature)
		val b = interpolate((lower._1, lower._2.blue), (upper._1, upper._2.blue), temperature)
		Color(r, g, b)
	}

	private[util] def findStartingPoints(x: Double) = {
		findBiggerValueIndex(x) match {
			case -1 => (datapoints.init.last, datapoints.last)
			case 0 => (datapoints.head, datapoints.tail.head)
			case default => (datapoints(default - 1), datapoints(default))
		}
	}

	private[util] def findBiggerValueIndex(x: Double) = {
		datapoints.indexWhere(_._1 >= x)
	}

	private[util] def interpolate(p0: RgbPoint, p1: RgbPoint, x: Double) = {
		val interpolated = p0._2 + (x - p0._1) * (p1._2 - p0._2) / (p1._1 - p0._1)
		val int = math.round(interpolated).toInt
		int match {
			case tooBig if tooBig > 255 => 255
			case tooSmall if tooSmall < 0 => 0
			case default => default
		}
	}

}

object ColorInterpolationUtil {

	private type RgbPoint = (Double, Int)

}
