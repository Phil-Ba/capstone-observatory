package observatory.util

import observatory.Location

import scala.math._

/**
	* Created by philba on 4/23/17.
	*/
object SlipperyMap {

	case class Tile(x: Int, y: Int, z: Int) {
		def toLatLon = new LatLonPoint(
			toDegrees(atan(sinh(Pi * (1.0 - 2.0 * y.toDouble / (1 << z))))),
			x.toDouble / (1 << z) * 360.0 - 180.0,
			z)

		def toLocation = Location(
			toDegrees(atan(sinh(Pi * (1.0 - 2.0 * y.toDouble / (1 << z))))),
			x.toDouble / (1 << z) * 360.0 - 180.0)

		def toURI = new java.net.URI("http://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png")
	}

	case class LatLonPoint(lat: Double, lon: Double, z: Int) {
		def toTile = Tile(
			((lon + 180.0) / 360.0 * (1 << z)).toInt,
			((1 - log(tan(toRadians(lat)) + 1 / cos(toRadians(lat))) / Pi) / 2.0 * (1 << z)).toInt, z)
	}

}
