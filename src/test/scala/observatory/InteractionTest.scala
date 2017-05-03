package observatory

import observatory.util.{SlipperyMap, TestDataUtil}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.reflect.io.Path

class InteractionTest extends FunSuite with Checkers {

	val grads = Seq(
		(60D, Color(255, 255, 255)),
		(32D, Color(255, 0, 0)),
		(12D, Color(255, 255, 0)),
		(0D, Color(0, 255, 255)),
		(-15D, Color(0, 0, 255)),
		(-27D, Color(255, 0, 255)),
		(-50D, Color(33, 0, 107)),
		(-60D, Color(0, 0, 0))
	)

	test("GenerateTile from real dataset") {

		def imgFunction(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
			val tileImage = Interaction.tile(data, grads, zoom, x, y)
			Path(s"src/generated/resources/temperatures/$year/$zoom").createDirectory()
			tileImage.output(s"src/generated/resources/temperatures/$year/$zoom/$x-$y.png")
		}

		val data: Seq[(Location, Double)] = TestDataUtil.fetchTestDataForYear(1975)
		val yearlyData = Seq((1975, data))
		Interaction.generateTile(yearlyData, imgFunction, 1)
	}

	test("yxc") {
		val temperatures = Seq((Location(45.0, -90.0), 20.0), (Location(45.0, 90.0), 0.0), (Location(0.0, 0.0), 10.0),
			(Location(-45.0, -90.0), 0.0), (Location(-45.0, 90.0), 20.0))
		val testLocation = SlipperyMap.LatLonPoint(84.92832092949963, -180.0, 0).toTile

		val tile = Interaction.tile(temperatures, grads, 0, 0, 0)


	}

}
