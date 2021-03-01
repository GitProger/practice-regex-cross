package solver.hexagon

import kotlin.math.*
import java.io.File

import solver.row.*

@ExperimentalUnsignedTypes
open class Hexagon(private val size: Int) {
    var regexps = MutableList(3) { MutableList(size * 2 - 1) { Row() } }
    var board = MutableList(size * 2 - 1) { i ->
        MutableList(minOf(size + i, 3 * size - 2 - i)) { '?' }
    }

    enum class Dir { RIGHT, LEFT_UP, LEFT_DOWN }
    data class Cell(var row: Int = 0, var col: Int = 0)

    private fun rowSize(row: Int) = board[row].size

    fun solve() {
        var progress = true
        while (progress) {
            progress = false
            for (i in 0 until board.size)
                for (j in 0 until rowSize(i))
                    progress = process(Cell(i, j)) || progress
        }
    }

    fun process(cell: Cell): Boolean {
        var progress = false
        var result = (1u shl alphabet) - 1u
        for (dir in Dir.values()) {
            val pos = transpose(dir, cell)
            result = result and regexps[dir.ordinal][pos.row].charOr(pos.col)
        }

        for (dir in Dir.values()) {
            val pos = transpose(dir, cell)
            progress = progress or regexps[dir.ordinal][pos.row].setChars(result, pos.col)
        }
        if (result.toString(2).count { it == '1' } == 1)
            board[cell.row][cell.col] = 'A' + result.toString(2).let { it.lastIndex - it.indexOf('1') }
        return progress
    }

    private fun transpose(dir: Dir, cell: Cell) = when (dir) {
        Dir.RIGHT -> cell
        Dir.LEFT_UP -> {
            val i = cell.col + max(size - 1 - cell.row, 0)
            val j = rowSize(i) - 1 - cell.row + max(size - 1 - i, 0)
            Cell(i, j)
        }
        Dir.LEFT_DOWN -> {
            val i = 2 * size - 2 - (cell.col + max(cell.row - size + 1, 0))
            val j = cell.row - max(size - 1 - i, 0)
            Cell(i, j)
        }
    }


    fun readFromFile(fileName: String) {
        val lines = mutableListOf<String>()
        File(fileName).inputStream().bufferedReader().forEachLine {
            if (it.isNotBlank()) lines.add(it.trim())
        }
        var curStr = 0
        for (i in 0..2)
            for (j in 0 until regexps[i].size) {
                val regex = lines[curStr++]
                regexps[i][j] = Row.fromRegex(regex, board[j].size)
            }
    }

    fun putToFile(fileName: String) {
        val result = File(fileName)
        result.writeText("")
        val longest = 4 * size - 3
        for (i in 0 until board.size) {
            val spaces = (longest - (2 * rowSize(i) - 1)) / 2
            val text = " ".repeat(spaces) +
                    (0 until rowSize(i)).toList()
                        .map { board[i][it].toString() }
                        .joinToString(" ") +
                    "\n"
            result.appendText(text)
        }
    }
}
