package observatory.util

import observatory.Location

/**
  * Created by pbayer on 25/04/2017.
  */
class ConversionUtil(val baseWidth: Int = 360, val baseHeight: Int = 180) {

  private val startLon = -baseWidth / 2
  private val startLat = baseHeight / 2

  def pixelToGps(x: Int, y: Int, scale: Int = 1) = {
    Location(startLat - y / scale, x / scale + startLon)
  }

}
