package solver.rectangle

import solver.Figure
import solver.row.*

class Rectangle(private val height: Int, private val width: Int) : Figure() {
    override var regexps = MutableList(2) {
        MutableList(if (it == 0) height else width) { Row() }
    }
    override var board = MutableList(height) { MutableList(width) { '?' } }

    override val directions = listOf("RIGHT", "DOWN")

    override fun transposeInverse(dir: String, cell: Cell) = transpose(dir, cell)

    override fun transpose(dir: String, cell: Cell) = when (dir) {
        "RIGHT" -> cell
        "DOWN" -> Cell(cell.col, cell.row)
        else -> throw IllegalArgumentException("$dir is not correct direction for rectangle")
    }

    override fun setLine(cell: Cell, dir: String, line: String) {
        for (i in line.indices) {
            when (dir) {
                "RIGHT" -> board[cell.row][i] = line[i]
                "DOWN" -> board[i][cell.col] = line[i]
                else -> throw IllegalArgumentException("$dir is not correct direction for rectangle")
            }
        }
    }

    override fun getLine(cell: Cell, dir: String): String {
        return when (dir) {
            "RIGHT" -> board[cell.row].joinToString("")
            "DOWN" -> board.indices.map { board[it][cell.col] }.joinToString("")
            else -> throw IllegalArgumentException("$dir is not correct direction for rectangle")
        }
    }

    override fun getLines(dir: String): List<String> {
        return when (dir) {
            "RIGHT" -> (0 until height).map { i -> board[i].joinToString("") }
            "DOWN" -> (0 until width).map { j ->
                (0 until height).map { i -> board[i][j] }.joinToString("")
            }
            else -> throw IllegalArgumentException("$dir is not correct direction for rectangle")
        }
    }

    override fun toString(): String {
        var s = ""
        for (i in 0 until height) {
            val text = (0 until width).toList().joinToString(" ") { board[i][it].toString() } + "\n"
            s += text
        }
        return s
    }
}
