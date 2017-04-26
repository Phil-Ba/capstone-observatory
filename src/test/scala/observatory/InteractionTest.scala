package observatory

import observatory.util.TestDataUtil
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.reflect.io.Path

class InteractionTest extends FunSuite with Checkers {


	test("GenerateTile from real dataset") {
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

		def imgFunction(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
			val tileImage = Interaction.tile(data, grads, zoom, x, y)
			Path(s"target/temperatures/$year/$zoom").createDirectory()
			tileImage.output(s"target/temperatures/$year/$zoom/$x-$y.png")
		}

    val data: Seq[(Location, Double)] = TestDataUtil.fetchTestDataForYear(1975)
    val yearlyData = Seq((1975, data))
		val t1 = System.nanoTime
    Interaction.generateTiles(yearlyData, imgFunction, 3)
		val duration = (System.nanoTime - t1) / 1e9d
		println(duration)
	}

}
