package observatory.util

import observatory.{Location, Visualization}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by pbayer on 25/04/2017.
  */
class ConversionUtilTest extends FunSuite with Matchers with TableDrivenPropertyChecks {

  test("pixelToGps") {
    val scale = 2
    val baseWidth = Visualization.baseWidth
    val baseHeight = Visualization.baseHeight
    val testData = Table(
      ("x", "y", "expectedLocation"),
      //img top left
      (0, 0, Location(baseHeight / 2, -baseWidth / 2)),
      //top right
      (baseWidth, 0, Location(baseHeight / 2, baseWidth / 2)),
      //img center
      (baseWidth / 2, baseHeight / 2, Location(0, 0)),
      //img bottom left
      (0, baseHeight, Location(-baseHeight / 2, -baseWidth / 2)),
      //img right bottom
      (baseWidth, baseHeight, Location(-baseHeight / 2, baseWidth / 2))
    )

    forEvery(testData) { (x, y, expectedLocation) =>
      ConversionUtil.pixelToGps(x, y, scale) shouldBe expectedLocation
    }
  }

}
