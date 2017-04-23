package observatory.util

import org.slf4j.LoggerFactory
import org.slf4j.event.Level

/**
	* Created by philba on 4/12/17.
	*/
object Profiler {
	val logger = LoggerFactory.getLogger(Profiler.getClass)

	def runProfiled[T](msg: String, level: Level = Level.INFO)(action: => T): T = {
		val log = getLogger(level)
		log("{} started.", msg)
		val t1 = System.nanoTime
		val t = action
		val duration = math.floor((System.nanoTime - t1) * 100 / (1e9D * 60)) / 100
		log(msg + " took {} minutes.", duration)
		t
	}

	private def getLogger(level: Level) = {
		level match {
			case Level.INFO => logger.info(_: String, _: Any)
			case Level.DEBUG => logger.debug(_: String, _: Any)
			case Level.TRACE => logger.trace(_: String, _: Any)
			case Level.WARN => logger.warn(_: String, _: Any)
			case Level.ERROR => logger.error(_: String, _: Any)
		}
	}

}
