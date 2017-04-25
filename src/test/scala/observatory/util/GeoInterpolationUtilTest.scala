package observatory.util

import observatory.Location
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import org.scalacheck.{Gen, Shrink}
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.prop.PropertyChecks

/**
	* Created by philba on 4/25/17.
	*/
class GeoInterpolationUtilTest extends FunSuite with PropertyChecks with Matchers {

	implicit val noShrink: Shrink[Any] = Shrink.shrinkAny
	implicit val noShrink2: Shrink[Double] = Shrink.shrinkAny

	val latLon = for (n <- Gen.choose(-90D, 90D)) yield {
		n
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
