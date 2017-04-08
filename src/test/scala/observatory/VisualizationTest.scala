package observatory


import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class VisualizationTest extends FunSuite with Checkers {

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

}
