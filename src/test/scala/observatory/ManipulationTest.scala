package observatory

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class ManipulationTest extends FunSuite with Matchers {

	test("average") {
		val testData = List(List((Location(45.0, -90.0), 10.0), (Location(0.0, 90.0), 15.0)),
			List((Location(0.0, -90.0), 11.0), (Location(45.0, 90.0), 14.0)))

		val avgFunction = Manipulation.average(testData)

		avgFunction(89, -180) shouldBe 12.015402247240784
	}

}