package observatory.util

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import observatory.{Extraction, Location}

import scala.reflect.io.Path

/**
  * Created by pbayer on 26/04/2017.
  */
object TestDataUtil {

  private def createTestDataSerialized(year: Int): Unit = {
    val records = Extraction
      .locationYearlyAverageRecords(Extraction.locateTemperatures(year, "/stations.csv", s"/$year.csv"))

    val data: Seq[(Location, Double)] = records.toSeq
    val fos = new FileOutputStream(s"src/test/resources/$year.ser")
    val oos = new ObjectOutputStream(fos)
    oos.writeObject(data)
    oos.close()
  }

  def fetchTestDataForYear(year: Int): Seq[(Location, Double)] = {
    val serFileLocation = s"src/test/resources/$year.ser"
    val serFile = Path(serFileLocation)
    if (serFile.exists == false) {
			createTestDataSerialized(year)
    }

    val fis = new FileInputStream(serFile.jfile)
    val ois = new ObjectInputStream(fis)
    ois.readObject().asInstanceOf[Seq[(Location, Double)]]
  }

}
