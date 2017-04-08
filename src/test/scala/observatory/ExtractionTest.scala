package observatory

import java.time.LocalDate

import observatory.Extraction.{Station, Temperature}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class ExtractionTest extends FunSuite with Matchers with TableDrivenPropertyChecks {

	test("locateTemperatures") {
		val result = Extraction.locateTemperatures(1975, "/stationsTest.csv", "/1975test.csv")

		val expected = (LocalDate.of(1975, 1, 2), Location(70.933, -8.667), -7.388888888888889)
		result should contain(expected)
	}

	test("stations") {
		val stationsData = Extraction.readStationsData("/stationsTest.csv")
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

	test("temperature") {
		val temperatureData = Extraction.readTemperatureData("/2015.csv")
			.collect()
		temperatureData.exists(_.stn == 7005) shouldBe false
		val temp = Temperature(Some(13730), None, 12, 24, 34.0)
		temperatureData should contain(temp)
	}


	test("locationYearlyAverageRecords") {

		val input: scala.Iterable[(LocalDate, Location, Double)] = Seq(
			(LocalDate.now(), Location(1, 1), 1.0),
			(LocalDate.now(), Location(1, 1), 5.0),
			(LocalDate.now(), Location(1, 1), 3.0),
			(LocalDate.now(), Location(1, 2), 3.0),
			(LocalDate.now(), Location(2, 1), 3.0)
		)
		val result = Extraction.locationYearlyAverageRecords(input)
		result should contain((Location(1, 1), 3.0))
		result should have size 3
	}

}