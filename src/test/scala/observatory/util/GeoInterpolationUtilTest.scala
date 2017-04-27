package observatory.util

import observatory.Location
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import org.scalacheck.{Gen, Shrink}
import org.scalatest.prop.{PropertyChecks, TableDrivenPropertyChecks}
import org.scalatest.{FunSuite, Matchers}

/**
	* Created by philba on 4/25/17.
	*/
class GeoInterpolationUtilTest extends FunSuite with PropertyChecks with TableDrivenPropertyChecks with Matchers {

	implicit val noShrink: Shrink[Any] = Shrink.shrinkAny
	implicit val noShrink2: Shrink[Double] = Shrink.shrinkAny

	val latLon = for (n <- Gen.choose(-90D, 90D)) yield {
		n
	}

	test("test calculation returns sensible results") {
		val testData = Table(
			("lat1", "lon1", "lat2", "lon2", "expected"),
			(80, 10, -80, -10, 17830.336),
			(75, 10, -80, -75, 17943.259),
			(80, 10, 80, -10, 384.385),
			(23, 10, 40, -10, 2665.766)
		)
		forEvery(testData) { (lat1, lon1, lat2, lon2, expected) =>
			//			GeoInterpolationUtil.approximateDistanceOpti(lat1, lon1, Location(lat2, lon2)) shouldBe expected +- 0.01
			//			GeoInterpolationUtil.approximateDistance(Location(lat1, lon1), Location(lat2, lon2)) shouldBe expected
			// +- 0.01
			GeoInterpolationUtil.approximateDistance(OptimizedLocation(Location(lat1, lon1)), Location(lat2, lon2)) shouldBe
				expected +- 0.09
			//		GeoInterpolationUtil.approximateDistanceOpti(80, 10, Location(-80, -10)) shouldBe 17825.31355471719 +- 0.
		}
		//		GeoInterpolationUtil.approximateDistanceOpti(Location(80,10),Location(-80,-10)) shouldBe 17830
	}


	test("test version with optimized locations") {
		forAll(latLon, latLon, latLon, latLon) { (lat1: Double, lon1: Double, lat2: Double, lon2: Double) => {
			val location1 = Location(lat1, lon1)
			val location2 = Location(lat2, lon2)
			GeoInterpolationUtil.approximateDistance(OptimizedLocation(location1), location2) shouldBe
				GeoInterpolationUtil.approximateDistance(lat1, lon1, location2) +- 0.0000001
		}
		}
	}

	test("test optimized version") {
		forAll(latLon, latLon, latLon, latLon) { (lat1: Double, lon1: Double, lat2: Double, lon2: Double) => {
			val location1 = Location(lat1, lon1)
			val location2 = Location(lat2, lon2)
			val distance = GeoInterpolationUtil.approximateDistance(lat1, lon1, location2)
			val distance2 = GeoInterpolationUtil.approximateDistanceOpti(lat1, lon1, location2)
			distance2 shouldBe distance +- 0.0000001
		}
		}
	}

}
