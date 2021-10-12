package generator

import generator.ranging.estimateCost
import solver.hexagon.Hexagon
import kotlin.math.exp

const val MAX_TEMPERATURE = 10_000
const val AVERAGE_COST: Double = 1.0

fun generateBoard(h: Hexagon, chars: List<Char>) {
    val board = h.board
    board.forEach { row -> row.indices.forEach { row[it] = chars.random() } }
    // simulated annealing
    for (temperature in MAX_TEMPERATURE downTo 1) {
        val cell = board.indices.random().let { row -> Hexagon.Cell(row, board[row].indices.random()) }
        val prevChar = board[cell.row][cell.col]
        val prevCost = Hexagon.Dir.values().sumOf { estimateCost(h.getLine(it, cell)) }
        board[cell.row][cell.col] = chars.random()
        val curCost = Hexagon.Dir.values().sumOf { estimateCost(h.getLine(it, cell)) }

        //the probability definition should probably be changed
        val probability = exp(-(curCost - prevCost) / AVERAGE_COST * MAX_TEMPERATURE / 2 / temperature)
        if (curCost > prevCost && probability < Math.random()) {
            // revert changes
            board[cell.row][cell.col] = prevChar
        }
    }
}
