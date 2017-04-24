package observatory

case class Location(lat: Double, lon: Double) {
	//	lazy val latRadians: Double = lat.toRadians
	//	lazy val latSinus: Double = math.sin(latRadians)
	//	lazy val latCosinus: Double = math.cos(latRadians)
	//	lazy val lonRadians: Double = lon.toRadians
}

case class Color(red: Int, green: Int, blue: Int)

