package solver.hexagon

import solver.Figure
import solver.row.*
import kotlin.math.*
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class Hexagon(private val size: Int) : Figure() {
    override var regexps = MutableList(3) { MutableList(size * 2 - 1) { Row() } }
    override var board = MutableList(size * 2 - 1) { i ->
        MutableList(minOf(size + i, 3 * size - 2 - i)) { '?' }
    }

    override val directions = listOf("LEFT_UP", "RIGHT", "LEFT_DOWN")

    override fun getLine(cell: Cell, dir: String): String {
        val row = transpose(dir, cell).row
        val transposedIndices = board[row].indices
        val indices = transposedIndices.map { transposeInverse(dir, Cell(row, it)) }
        return indices.map { board[it.row][it.col] }.joinToString("")
    }

    private fun transposeInverse(dir: String, cell: Cell) = transpose(
        when (dir) {
            "RIGHT" -> "RIGHT"
            "LEFT_UP" -> "LEFT_DOWN"
            "LEFT_DOWN" -> "LEFT_UP"
            else -> throw IllegalArgumentException("$dir is not correct direction for hexagon")
        }, cell
    )

    override fun transpose(dir: String, cell: Cell) = when (dir) {
        "RIGHT" -> cell
        "LEFT_UP" -> {
            val i = cell.col + max(size - 1 - cell.row, 0)
            val j = rowSize(i) - 1 - cell.row + max(size - 1 - i, 0)
            Cell(i, j)
        }
        "LEFT_DOWN" -> {
            val i = 2 * size - 2 - (cell.col + max(cell.row - size + 1, 0))
            val j = cell.row - max(size - 1 - i, 0)
            Cell(i, j)
        }
        else -> throw IllegalArgumentException("$dir is not correct direction for hexagon")
    }

    override fun toString(): String {
        val result = StringBuilder()
        val longest = 4 * size - 3
        for (i in 0 until board.size) {
            val spaces = (longest - (2 * rowSize(i) - 1)) / 2
            val text = " ".repeat(spaces) +
                    (0 until rowSize(i)).toList()
                        .map { board[i][it] }
                        .joinToString(" ") +
                    "\n"
            result.append(text)
        }
        return result.toString()
    }
}
