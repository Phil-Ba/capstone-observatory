package observatory


import observatory.util.TestDataUtil
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class VisualizationTest extends FunSuite with Matchers with TableDrivenPropertyChecks {

	test("predictTemperature") {
		val datapoints = Seq(
			(Location(1, 2), 3D),
			(Location(2, 2), 4D),
			(Location(1, 3), 6D),
			(Location(2, 1), 1D)
		)
		val result = Visualization.predictTemperature(datapoints, Location(2, 3))
		println(result)
	}

	test("predictTemperature real dataset") {
		val records = Extraction
			.locationYearlyAverageRecords(Extraction.locateTemperatures(1975, "/stations.csv", "/1975.csv"))

		val t1 = System.nanoTime
		val result = Visualization.predictTemperature(records, Location(2, 3))
		val duration = (System.nanoTime - t1) / 1e9d
		println(duration)
		println(result)
	}

	test("visualize real dataset") {
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

		val year = 1975
    val data = TestDataUtil.fetchTestDataForYear(year)

		val t1 = System.nanoTime
    val result = Visualization.visualize(data, grads, 1)
		val duration = (System.nanoTime - t1) / 1e9d
		result.output(s"vizualImg$year.png")
		println(duration)
		println(result)

	}

}
