package observatory.util

/**
	* Created by philba on 5/14/17.
	*/
object TilesUtil {

	def xyValuesForZooms(zoom: Int*): Seq[(Int, Int, Int)] = {
		for {
			zoomLevel <- zoom
		} yield {
			val tiles = Math.round(Math.pow(2, 2 * zoomLevel) / Math.pow(2, zoomLevel)).toInt
			val xyz = for {
				x <- 0 until tiles
				y <- 0 until tiles
			} yield {
				(x, y, zoomLevel)
			}
			xyz
		}
	}.flatten


}
