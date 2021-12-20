package generator

import generator.ranging.estimateCost
import solver.Figure
import solver.Figure.Cell
import solver.rectangle.Rectangle
import java.lang.Math.random
import kotlin.math.*

const val MAX_TEMPERATURE = 10_000
const val Q: Double = 1e6

fun computeCost(f: Rectangle): Int {
    var ans = 0
    for (i in f.board.indices) ans += estimateCost(f.getLine(Cell(i, 0), "RIGHT"))
    for (i in f.board[0].indices) ans += estimateCost(f.getLine(Cell(0, i), "DOWN"))
    return ans
}

fun generateBoard(f: Figure, chars: List<Char>) {
    val board = f.board
    for (row in board) {
        for (i in row.indices) {
            if (row[i] == '?') row[i] = chars.random()
        }
    }
    // simulated annealing
    for (temperature in MAX_TEMPERATURE downTo 1) {
        val cell = board.indices.random().let { row -> Cell(row, board[row].indices.random()) }
        val prevChar = board[cell.row][cell.col]
        val oldCost = f.directions.sumOf { estimateCost(f.getLine(cell, it)) }
        board[cell.row][cell.col] = chars.random()
        val newCost = f.directions.sumOf { estimateCost(f.getLine(cell, it)) }

        val probability = exp(-(oldCost - newCost) * Q / temperature)
        if (oldCost != newCost) {
            println("$temperature: oldCost is $oldCost, newCost is $newCost")
        }
        if (oldCost < newCost || probability > random()) {
            println("Changes accepted")
            println("New global cost is ${computeCost(f as Rectangle)}")
            continue
        }
        // revert changes
        board[cell.row][cell.col] = prevChar
    }
}
