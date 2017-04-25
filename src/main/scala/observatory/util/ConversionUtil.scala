package observatory.util

import observatory.Location
import observatory.Visualization.{baseHeight, baseWidth}

/**
  * Created by pbayer on 25/04/2017.
  */
object ConversionUtil {

  def pixelToGps(x: Int, y: Int, scale: Int = 1) = {
    Location(baseHeight / 2 - (y / 2), (x / 2) - baseWidth / 2)
  }

}
