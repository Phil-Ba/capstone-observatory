package observatory

import observatory.Extraction.{Station, Temperature}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class ExtractionTest extends FunSuite with Matchers with TableDrivenPropertyChecks {

	test("stations test") {
		val stationsData = Extraction.readStationsData("/stations.csv")
			.collect()
		stationsData.exists(_.stn == 7005) shouldBe false

		val station = Station(Some(725976), Some(94285), Some(42.167), Some(-120.400))
		stationsData should contain(station)
	}

	test("convertFarenheitToCelsius") {
		val conversionTable = Table(
			("f", "c"),
			(50.0, 10.00),
			(15.8, -9.00),
			(24.8, -4.00),
			(68.0, 20.00)
		)
		forAll(conversionTable) { (f, c) ⇒
			Extraction.convertFarenheitToCelsius(f) shouldBe c
		}
	}

	test("temperature test") {
		val temperatureData = Extraction.readTempratureData("/2015.csv")
			.collect()
		temperatureData.exists(_.stn == 7005) shouldBe false
		val temp = Temperature(Some(13730), None, 12, 24, 34.0)
		temperatureData should contain(temp)
	}


}