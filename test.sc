import observatory.Interaction

def convertFarenheitToCelsius(f: Double) = (f - 32) * 5 / 9
convertFarenheitToCelsius(18.7)

None == None
None.equals(None)

1 until 1 foreach println
"abc"
1 to 1 foreach println

for {
	zoom <- 0 to 1
	tiles = Math.round(Math.pow(2, 2 * zoom) / 2).toInt
	x <- 0 until tiles
	y <- 0 until tiles
} yield {
	(zoom, x, y)
}

Interaction.generateInputs2(1, (3, ""))
	.foreach(inp => println(s"z:${inp._2} | x:${inp._3} y:${inp._4} "))