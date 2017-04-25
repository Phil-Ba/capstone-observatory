package observatory

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import scala.reflect.io.Path

class InteractionTest extends FunSuite with Checkers {


	def createTestDataSerialized(year: Int): Unit = {
		val records = Extraction
			.locationYearlyAverageRecords(Extraction.locateTemperatures(year, "/stations.csv", s"/$year.csv"))

		val data: Seq[(Int, Iterable[(Location, Double)])] = Seq((year, records))
		val fos = new FileOutputStream(s"src/test/resources/$year.ser")
		val oos = new ObjectOutputStream(fos)
		oos.writeObject(data)
		oos.close()
	}

	test("GenerateTile from real dataset") {
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

		val serFileLocation = "src/test/resources/1975.ser"
		val serFile = Path(serFileLocation)
		if (serFile.exists == false) {
			createTestDataSerialized(1975)
		}

		val fis = new FileInputStream(serFile.jfile)
		val ois = new ObjectInputStream(fis)
		val data: Seq[(Int, Iterable[(Location, Double)])] = ois.readObject()
			.asInstanceOf[Seq[(Int, Iterable[(Location, Double)])]]

		def imgFunction(year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) = {
			val tileImage = Interaction.tile(data, grads, zoom, x, y)
			Path(s"target/temperatures/$year/$zoom").createDirectory()
			tileImage.output(s"target/temperatures/$year/$zoom/$x-$y.png")
		}

		val t1 = System.nanoTime
		Interaction.generateTiles(data, imgFunction, 3)
		val duration = (System.nanoTime - t1) / 1e9d
		println(duration)
	}

}
