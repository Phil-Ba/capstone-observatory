package observatory.util

import observatory.Color
import observatory.util.InterpolationUtil.RgbPoint

/**
	* Created by philba on 4/11/17.
	*/
class InterpolationUtil(datapointsInput: Seq[(Double, Color)]) {

	val datapoints = datapointsInput.sortBy(_._1)

	def interpolate(x: Double): Unit = {
		val startingPoints = findStartingPoints(x)
		val lower = startingPoints._1
		val upper = startingPoints._2
		val r = interpolate((lower._1, lower._2.red), (upper._1, upper._2.red), x)
		val g = interpolate((lower._1, lower._2.green), (upper._1, upper._2.green), x)
		val b = interpolate((lower._1, lower._2.blue), (upper._1, upper._2.blue), x)
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
		interpolated.toInt
	}

}

object InterpolationUtil {

	private type RgbPoint = (Double, Int)

}
