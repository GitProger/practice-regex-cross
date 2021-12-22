package generator

import generator.ranging.FigureWithCost
import solver.Figure
import java.lang.Math.random
import kotlin.math.*

const val MAX_TEMPERATURE = 10_000
const val Q: Double = 1e3

fun prepareBoard(f: Figure) {
    f.setLines("RIGHT", listOf(0 to "GREETINGS".substring(0 until f.rowSize(0))))
    val chars = ('A'..'Z').toList()
    f.fillRandomly(chars)
    generateBoard(FigureWithCost(f), chars)
}

private fun generateBoard(f: FigureWithCost, chars: List<Char>) {
    // simulated annealing
    for (temperature in MAX_TEMPERATURE downTo 1) {
        val cell = f.randomCell()
        val prevChar = f[cell]
        val oldCost = f.cost()
        f[cell] = chars.random()
        val newCost = f.cost()

        val probability = exp(-(oldCost - newCost) * Q / temperature)
        if (oldCost != newCost) {
            println("$temperature: oldCost is $oldCost, newCost is $newCost")
        }
        if (oldCost < newCost || probability > random()) {
            println("Changes accepted")
            println(f.cost())
            continue
        }
        // revert changes
        f[cell] = prevChar
    }
}
