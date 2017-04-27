package observatory.util

import java.lang.Math.pow

import observatory.Location

import scala.math._

/**
	* Created by philba on 4/24/17.
	*/
object GeoInterpolationUtil {

	private val R = 6372.8 //radius in km

	def approximateDistance(lat: Double, lon: Double, location2: Location): Double = {
		val dLat = (lat - location2.lat).toRadians
		val dLon = (lon - location2.lon).toRadians

		val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(lat.toRadians) * cos(location2.lat.toRadians)
		val c = 2 * asin(sqrt(a))
		R * c
	}

	def approximateDistance(location1: Location, location2: Location): Double = {
		val dLat = (location1.lat - location2.lat).toRadians
		val dLon = (location1.lon - location2.lon).toRadians

		val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(location1.lat.toRadians) * cos(location2.lat.toRadians)
		val c = 2 * asin(sqrt(a))
		R * c
	}

	def approximateDistance(location1: OptimizedLocation, location2: Location): Double = {
		val lat1Rad = location2.lat.toRadians
		val dLat = location1.latRad - lat1Rad
		val dLon = location1.lonRad - location2.lon.toRadians

		val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * location1.cosLat * cos(lat1Rad)
		val c = 2 * asin(sqrt(a))
		R * c
	}

	case class OptimizedLocation(location: Location) {
		val latRad = location.lat.toRadians
		val lonRad = location.lon.toRadians
		val cosLat = cos(latRad)
	}

	type LatSinusCosinusLonRadians = (Double, Double, Double)

	def approximateDistanceOpti(location1: LatSinusCosinusLonRadians,
															location2: Location): Double = {
		val lat2 = location2.lat.toRadians
		val lon2 = location2.lon.toRadians

		val a = location1._2 * sin(lat2)
		val b = location1._3 * cos(lon2) * cos(lon2 - location1._1)
		val c = acos(a + b)
		//		val c = Optimizer.acos(a + b)
		R * c
	}

	def approximateDistanceOpti(lat: Double, lon: Double, location2: Location): Double = {
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
