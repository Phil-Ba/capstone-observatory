package observatory

import observatory.util.{Profiler, TestDataUtil}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.reflect.io.Path

class InteractionTest extends FunSuite with Checkers {

  test("GenerateTiles from real dataset") {

    def imgFunction(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
      val image = s"src/generated/resources/temperatures/$year/$zoom/$x-$y.png"
      if (!Path(image).exists) {
        Profiler.runProfiled(s"Generating image tile for $zoom | ($x,$y)") {
          val tileImage = Interaction.tile(data, Main.grads, zoom, x, y)
          Path(s"src/generated/resources/temperatures/$year/$zoom").createDirectory()
          tileImage.output(image)
        }
      }
    }

    for (yearToVisualize <- 1975 to 2015) {
      val dataForYear = TestDataUtil.fetchTestDataForYear(yearToVisualize)
      val yearlyData = Seq((yearToVisualize, dataForYear))
      Profiler.runProfiled(s"Generating image tile for $yearToVisualize") {
        Interaction.generateTiles(yearlyData, imgFunction, 3)
      }
    }

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

}
