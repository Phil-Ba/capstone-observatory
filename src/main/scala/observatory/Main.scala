package observatory

import org.apache.spark.sql.SparkSession

import scala.collection.parallel.ForkJoinTaskSupport

object Main {
	//object Main extends App {

	import org.apache.log4j.{ConsoleAppender, Level, Logger, PatternLayout}

	lazy val fjPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

	def createFjPool(size: Int) = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(size))

	lazy val loggerConfig = {

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

	lazy val spark: SparkSession =
		SparkSession
			.builder()
			.appName("Time Usage")
			.config("spark.master", "local[4]")
			.getOrCreate()
}
