package observatory.util

import observatory.Color
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

/**
	* Created by philba on 4/11/17.
	*/
class ColorInterpolationUtilTest extends FunSuite with TableDrivenPropertyChecks with Matchers {

	test("interpolation") {
		val testData = Table(
			("p0", "p1", "x", "expected"),
			((1D, 1), (4D, 4), 3D, 3D),
			((1D, 1), (4D, 4), 8D, 8D),
			((1D, 5), (5D, 50), 3D, 28),
			((1D, 3), (4D, 12), 3D, 9D),
			((1D, 3), (4D, 12), 0.5D, 2D)
		)

		forEvery(testData) { (p0, p1, x, expected) => {
			new ColorInterpolationUtil(Nil).interpolate(p0, p1, x) shouldBe expected
		}
		}
	}

	test("findStartingPoints") {
		val seq = Seq[(Double, Color)](
			(2, null),
			(1, null),
			(4, null),
			(7, null),
			(5, null),
			(3, null),
			(6, null)
		)
		val testData = Table(
			("value", "expectedValues"),
			(0D, (1, 2)),
			(8D, (6, 7)),
			(4.5, (4, 5)),
			(1D, (1, 2)),
			(1.5, (1, 2))
		)

		forEvery(testData) { (value, expectedValues) => {
			val cut = new ColorInterpolationUtil(seq)

			val result = cut.findStartingPoints(value)

			withClue("lower bound") {
				result._1._1 shouldBe expectedValues._1
			}
			withClue("upper bound") {
				result._2._1 shouldBe expectedValues._2
			}
		}
		}
	}

}
