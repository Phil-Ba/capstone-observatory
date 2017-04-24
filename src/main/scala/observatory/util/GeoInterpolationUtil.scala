package observatory.util

import java.lang.Math.pow

import observatory.Location
import observatory.Visualization.R

import scala.math._

/**
	* Created by philba on 4/24/17.
	*/
object GeoInterpolationUtil {

	protected[observatory] def approximateDistance(lat: Double, lon: Double, location2: Location): Double = {
		val dLat = (lat - location2.lat).toRadians
		val dLon = (lon - location2.lon).toRadians

		val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(lat.toRadians) * cos(location2.lat.toRadians)
		val c = 2 * asin(sqrt(a))
		R * c
	}

	protected[observatory] def approximateDistanceOpti(lat: Double, lon: Double, location2: Location): Double = {
		val lat1 = lat.toRadians
		val lat2 = location2.lat.toRadians
		val lon1 = lon.toRadians
		val lon2 = location2.lon.toRadians


		val a = sin(lat1) * sin(lat2)
		val b = cos(lat1) * cos(lat2) * cos(lon2 - lon1)
		val c = acos(a + b)
		//		val c = Optimizer.acos(a + b)
		R * c
	}

}
