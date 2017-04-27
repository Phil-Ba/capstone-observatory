package observatory.viz

import java.lang.Math.pow

import monix.eval.Task
import monix.reactive.Observable
import observatory.util.GeoInterpolationUtil.OptimizedLocation
import observatory.util.{ColorInterpolationUtil, ConversionUtil, Profiler}
import observatory.{Color, Location, Main}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
	* 2nd milestone: basic visualization
	*/
object VisualizationGeneric {

	val R = 6372.8 //radius in km
	val p = 6
	val baseWidth: Int = 360
	val baseHeight: Int = 180

	Main.loggerConfig
	private val tempApproxPool = Main.createFjPool(4)
	private val pixelCalcPool = Main.createFjPool(2)

	def computeImgValues[T](temperatures: Iterable[(T, Double)],
													colors: Iterable[(Double, Color)],
													xyValues: Seq[(Int, Int)],
													distanceFunction: (T, Location) => Double,
													scale: Int = 1): Seq[((Int, Int), Color)] = {
    import monix.execution.Scheduler.Implicits.global
		Profiler.runProfiled("computeImgValues") {
			val cache = mutable.HashMap[Double, Color]()
			val conversionUtil = new ConversionUtil()
      val tempObserv = Observable.fromIterable(temperatures)

      //			val xyPar = xyValues.par
      //			xyPar.tasksupport = pixelCalcPool
			val util = new ColorInterpolationUtil(colors.toSeq)
      val tasks: Observable[((Int, Int), Color)] = Observable.fromIterable(xyValues)
				.mapAsync(5)(xy =>
          //					Task {
					//							Profiler.runProfiled(s"approxTemp for pixel $xy") {
          approxTemperature(tempObserv, conversionUtil.pixelToGps(xy._1, xy._2, scale),
            distanceFunction)
            .map(temp => {
							val color = cache.getOrElseUpdate(temp, util.interpolate(temp))
							(xy, color)
              //								})
            })
					//							}
        )
      val future = tasks.toListL.runAsync
      Await.result(future, Duration.Inf)
      //					})
      //			xyColors

		}
	}

	def mapToOptimizedLocations(temperatures: Iterable[(Location, Double)]): Iterable[(OptimizedLocation, Double)] =
		temperatures.map(temp => (OptimizedLocation(temp._1), temp._2))

  def approxTemperature[T](temperatures: Observable[(T, Double)],
                           locationToapprox: Location,
                           distanceFunction: (T, Location) => Double): Task[Double] = {
    //		val temperaturesPar = temperatures.par
    //		temperaturesPar.tasksupport = tempApproxPool
    //		val res: Observable[(Double, Double)] = Observable.fromIterable(temperatures)
		//		Profiler.runProfiled(s"approxTemp for location $locationToapprox") {
		val res = temperatures
			.map((locAndTemp: (T, Double)) => {
				//				.mapAsync(1000)((locAndTemp: (T, Double)) => Task {
				val locationDatapoint = locAndTemp._1
				val temp = locAndTemp._2
				val distance = distanceFunction(locationDatapoint, locationToapprox)
				val invWeight = 1 / pow(distance, p)
        (temp * invWeight, invWeight)
			})

    val aggregation = res.foldLeftL((0.0, 0.0))((t1, t2) => (t1._1 + t2._1, t1._2 + t2._2))
      .map(sums => sums._1 / sums._2)
    aggregation
		//		}
    //		val result = Await.result(aggregation.runAsync, Duration.Inf)
    //		result._1 / result._2
    //			.groupBy(_._1)
    //		res

    //			.groupBy(_._1)


    //		val v1 = result(1).aggregate(0.0D)(
    //			(sum: Double, values) => sum + values._2,
    //			(d1, d2) => d1 + d2)
    //		val v2 = result(2).aggregate(0.0D)(
    //			(sum: Double, values) => sum + values._2,
    //			(d1, d2) => d1 + d2)

    //		v1 / v2
	}

}

