package observatory

import observatory.util.{Profiler, TestDataUtil, TilesUtil}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

import scala.reflect.io.Path

@RunWith(classOf[JUnitRunner])
class Visualization2Test extends FunSuite with Checkers {

  test("generate dev image") {
    val yearStart = 1975
    val yearEnd = 1989

    val normals: Seq[Seq[(Location, Double)]] = for {
      year <- yearStart to yearEnd
    } yield {
      TestDataUtil.fetchTestDataForYear(year)
    }

    val averageGrid = Manipulation.average(normals)

    for (yearToVisualize <- 1975 to 2000) {
      val dataForYear = TestDataUtil.fetchTestDataForYear(yearToVisualize)
      val deviationGrid = Manipulation.deviation(dataForYear, averageGrid)

      val zooms = TilesUtil.xyValuesForZooms(0, 1, 2, 3)
      for (xyz <- zooms) {
        Profiler.runProfiled(s"generating deviation(zoom:${xyz._3} | x=${xyz._1}, y=${xyz._2})") {
          val image = s"src/generated/resources/deviations/$yearToVisualize/${xyz._3}/${xyz._1}-${xyz._2}.png"
          if (!Path(image).exists) {
            val tileImage = Visualization2.visualizeGridMonix(deviationGrid, Main.gradsDeviation, xyz._3, xyz._1, xyz._2)
            Path(s"src/generated/resources/deviations/$yearToVisualize/${xyz._3}").createDirectory()
            tileImage.output(image)
          }
        }
      }
    }
  }

}
