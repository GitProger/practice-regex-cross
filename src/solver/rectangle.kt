package solver.rectangle

import solver.BaseFigure
import solver.row.*

class Rectangle(private val height: Int, private val width: Int): BaseFigure() {
    override var regexps = MutableList(2) {
        MutableList(if (it == 0) height else width) { Row() } 
    }
    override var board = MutableList(height) { MutableList(width) { '?' } }

    override val directions = listOf("RIGHT", "DOWN")

    override fun transpose(dir: String, cell: Cell) = when (dir) {
        "RIGHT" -> cell
        "DOWN" -> Cell(cell.col, cell.row)
        else -> throw IllegalArgumentException("$dir is not correct direction for rectangle")
    }

    override fun getLine(cell: Cell, dir: String): String {
        if (dir == "RIGHT") return board[cell.row].joinToString("")
        if (dir == "DOWN") return board.indices.map { board[it][cell.col] }.joinToString("")
        throw IllegalArgumentException("$dir is not correct direction for rectangle")
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
