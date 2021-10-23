package generator

import generator.ranging.estimateCost
import solver.BaseFigure
import solver.BaseFigure.Cell
import kotlin.math.*

const val MAX_TEMPERATURE = 10_000
const val Q: Double = 100000000.0

fun generateBoard(f: BaseFigure, chars: List<Char>) {
    val board = f.board
    board.forEach { row -> row.indices.forEach { row[it] = chars.random() } }
    // simulated annealing
    for (temperature in MAX_TEMPERATURE downTo 1) {
        val cell = board.indices.random().let { row -> Cell(row, board[row].indices.random()) }
        val prevChar = board[cell.row][cell.col]
        val oldCost = f.directions.sumOf { estimateCost(f.getLine(cell, it)) }
        board[cell.row][cell.col] = chars.random()
        val newCost = f.directions.sumOf { estimateCost(f.getLine(cell, it)) }

        if (oldCost < newCost) continue
        //the probability definition should probably be changed
        val probability = exp(-(oldCost - newCost) * Q / temperature)
        // revert changes
        board[cell.row][cell.col] = prevChar
    }
}
