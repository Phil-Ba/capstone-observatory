package observatory

import observatory.util.{SlipperyMap, TestDataUtil}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.reflect.io.Path

class InteractionTest extends FunSuite with Checkers {

	test("GenerateTiles from real dataset") {

		def imgFunction(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
			val tileImage = Interaction.tile(data, Main.grads, zoom, x, y)
			Path(s"src/generated/resources/temperatures/$year/$zoom").createDirectory()
			tileImage.output(s"src/generated/resources/temperatures/$year/$zoom/$x-$y.png")
		}

		val data: Seq[(Location, Double)] = TestDataUtil.fetchTestDataForYear(1975)
		val yearlyData = Seq((1975, data))
		Interaction.generateTiles(yearlyData, imgFunction, 3)
	}

	test("GenerateTile from real dataset") {

		def imgFunction(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
			val tileImage = Interaction.tile(data, Main.grads, zoom, x, y)
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

		val tile = Interaction.tile(temperatures, Main.grads, 0, 0, 0)
	}

}
