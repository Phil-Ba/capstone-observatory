package observatory.util

import org.slf4j.LoggerFactory
import org.slf4j.event.Level

/**
	* Created by philba on 4/12/17.
	*/
object Profiler {
	val logger = LoggerFactory.getLogger(Profiler.getClass)


	def runProfiled[T](msg: String, level: Level = Level.INFO)(action: => T): T = {
		val t1 = System.nanoTime
		val t = action
		val duration = math.floor((System.nanoTime - t1) * 100 / (1e9D * 60)) / 100
		level match {
			case Level.INFO => logger.info(msg + " took {} minutes.", duration)
			case Level.DEBUG => logger.debug(msg + " took {} minutes.", duration)
			case Level.TRACE => logger.trace(msg + " took {} minutes.", duration)
			case Level.WARN => logger.warn(msg + " took {} minutes.", duration)
			case Level.ERROR => logger.error(msg + " took {} minutes.", duration)
		}
		t
	}

}
