package solver.figure

import kotlin.math.*
import java.io.File

import solver.row.*

open class BaseFigure {
    abstract enum class Dir {}
    data class Cell(var row: Int = 0, var col: Int = 0)

    var regexps = mutableListOf<MutableList<Row>>()
    var board = mutableListOf<MutableList<Char>>()

    protected fun rowSize(row: Int) = board[row].size

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
        var result = 1u shl alphabet - 1
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

    open protected abstract fun transpose(dir: Dir, cell: Cell)
    open abstract fun putToFile(fileName: String)

    fun readFromFile(fileName: String) {
        val lines = mutableListOf<String>()
        File(fileName).inputStream().bufferedReader().forEachLine {
            if (it.isNotBlank()) lines.add(it.trim())
        }
        var curStr = 0
        for (i in 0 until regexps.size)
            for (j in 0 until regexps[i].size) {
                val regex = lines[curStr++]
                regexps[i][j] = Row.fromRegex(regex, board[j].size)
            }
    }
}

