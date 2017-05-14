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
		val data = TestDataUtil.fetchTestDataForYear(1975)

		val result = Visualization.predictTemperature(data, Location(2, 3))
		println(result)
	}

	test("predictTemperature fake dataset") {
		val data = List((Location(45.0, -90.0), -41.195131039498634), (Location(-45.0, 0.0), 1.0))
		val img = Visualization.visualize(data, Main.grads)
		val pixel = img.pixel(90, 45)
		println(pixel.toColor)
	}

	test("color for fake dataset") {
		//		val data = List((Location(45.0,-90.0),-41.195131039498634), (Location(-45.0,0.0),1.0))
		val data = List((Location(45.0, -90.0), 61.09273383703933), (Location(-45.0, 0.0), 0.0))
		val temp = Visualization.predictTemperature(data, Location(45, -90))
	}

	test("visualize real dataset") {
		val year = 1975
		val data = TestDataUtil.fetchTestDataForYear(year)

		val result = Visualization.visualize(data, Main.grads, 1)
		result.output(s"vizualImg$year.png")

	}

}
