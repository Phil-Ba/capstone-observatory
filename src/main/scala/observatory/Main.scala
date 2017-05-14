package observatory

import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import scala.collection.parallel.ForkJoinTaskSupport

object Main {

	import org.apache.log4j.{ConsoleAppender, Level, Logger, PatternLayout}

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

	val gradsDeviation = Seq(
		(7D, Color(0, 0, 0)),
		(4D, Color(255, 0, 0)),
		(2D, Color(255, 255, 0)),
		(0D, Color(255, 255, 255)),
		(-2D, Color(0, 255, 255)),
		(-7D, Color(0, 0, 255))
	)

	lazy val fjPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

	def createFjPool(size: Int) = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(size))

	val loggerConfig = {

		val console = new ConsoleAppender
		//create appender
		//configure the appender
		val PATTERN = "[%d{HH:mm:ss}][%p][%c{1}] %m%n"
		console.setLayout(new PatternLayout(PATTERN))
		console.setThreshold(Level.ALL)
		console.activateOptions()
		//add appender to any Logger (here is root)
		Logger.getRootLogger.addAppender(console)
		Logger.getRootLogger.setLevel(Level.INFO)
		Logger.getRootLogger.setAdditivity(true)
		Logger.getRootLogger.getLoggerRepository.getLogger("observatory").setLevel(Level.INFO)
		Logger.getRootLogger.getLoggerRepository.getLogger("org.apache.spark").setLevel(Level.WARN)
		Logger.getRootLogger.getLoggerRepository.getLogger("org.spark_project").setLevel(Level.WARN)
	}

	/* This will return Long.MAX_VALUE if there is no preset limit */
	private val logger = LoggerFactory.getLogger(this.getClass)
	logger.info("Max memory(kb):{}", Runtime.getRuntime.maxMemory / 1000)
	logger.info("Free memory(kb):{}", Runtime.getRuntime.freeMemory / 1000)

	lazy val spark: SparkSession =
		SparkSession
			.builder()
			.appName("Time Usage")
			.config("spark.master", "local[4]")
			.getOrCreate()
}
