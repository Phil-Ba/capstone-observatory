package observatory.util

import org.slf4j.LoggerFactory
import org.slf4j.event.Level

/**
	* Created by philba on 4/12/17.
	*/
object Profiler {
	val logger = LoggerFactory.getLogger(Profiler.getClass)


	def runProfiled[T](msg: String, level: Level = Level.INFO)(action: => T) = {
		val t1 = System.nanoTime
		val t = action
		val duration = (System.nanoTime - t1) / 1e9D * 60
		level match {
			case Level.INFO => logger.info(msg + " took {} seconds.", duration)
			case Level.DEBUG => logger.debug(msg + " took {} seconds.", duration)
			case Level.TRACE => logger.trace(msg + " took {} seconds.", duration)
			case Level.WARN => logger.warn(msg + " took {} seconds.", duration)
			case Level.ERROR => logger.error(msg + " took {} seconds.", duration)
		}
		t
	}

}
