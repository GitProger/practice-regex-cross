package solver

import kotlin.math.*
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class Hexagon(private val size: Int) : Figure() {
    override val regexps = List(3) { Array(size * 2 - 1) { "" } }
    override val board = List(size * 2 - 1) { i ->
        Array(minOf(size + i, 3 * size - 2 - i)) { '?' }
    }

    override fun rowSize(dir: String, row: Int) = board[row].size

    override val directions = listOf("LEFT_UP", "RIGHT", "LEFT_DOWN")

    override fun setLine(cell: Cell, dir: String, line: String) {
        val row = transpose(dir, cell).row
        val transposedIndices = board[row].indices
        val indices = transposedIndices.map { transposeInverse(dir, Cell(row, it)) }
        for ((char, c) in line.toList().zip(indices)) {
            this[c] = char
        }
    }

    override fun getLine(cell: Cell, dir: String): String {
        val row = transpose(dir, cell).row
        val transposedIndices = board[row].indices
        val indices = transposedIndices.map { transposeInverse(dir, Cell(row, it)) }
        return indices.map { this[it] }.joinToString("")
    }

    override fun getLines(dir: String): List<String> {
        return (0 until 2 * size - 1).map { getLine(transposeInverse(dir, Cell(it, 0)), dir) }
    }

    override fun transposeInverse(dir: String, cell: Cell) = transpose(
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
            val j = rowSize(dir, i) - 1 - cell.row + max(size - 1 - i, 0)
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
            val spaces = (longest - (2 * board[i].size - 1)) / 2
            val text = " ".repeat(spaces) +
                    (0 until board[i].size).map { board[i][it] }.joinToString(" ") +
                    "\n"
            result.append(text)
        }
        return result.toString()
    }
}
