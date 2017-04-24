package observatory.util

/**
	* Created by philba on 4/23/17.
	*/
object Optimizer {

	def acos(i: Double) = {
		val negate = if (i < 0) {
			-1
		} else {
			0
		}
		val x = math.abs(i)
		var ret = -0.0187293
		ret = ret * x
		ret = ret + 0.0742610
		ret = ret * x
		ret = ret - 0.2121144
		ret = ret * x
		ret = ret + 1.5707288
		ret = ret * Math.sqrt(1.0 - x)
		ret = ret - 2 * negate * ret
		negate * 3.14159265358979 + ret
	}

}
