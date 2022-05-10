package solver

import java.io.File

abstract class Figure {
    abstract val directions: List<String>

    inner class Cell(var row: Int = 0, var col: Int = 0) {
        fun code() = col * board.size + row
    }

    abstract val regexps: List<Array<String>>
    abstract val board: List<Array<Char>>

    abstract fun rowSize(dir: String, row: Int): Int

    abstract fun setLine(cell: Cell, dir: String, line: String)
    abstract fun getLine(cell: Cell, dir: String): String
    abstract fun getLines(dir: String): List<String>

    abstract fun transpose(dir: String, cell: Cell): Cell
    abstract fun transposeInverse(dir: String, cell: Cell): Cell

    fun readFromFile(fileName: String) {
        val lines = mutableListOf<String>()
        File(fileName).inputStream().bufferedReader().forEachLine {
            if (it.isNotBlank()) lines.add(it.trim())
        }
        var curStr = 0
        for (i in 0 until regexps.size)
            for (j in 0 until regexps[i].size) {
                val regex = lines[curStr++]
                regexps[i][j] = regex
            }
    }

    fun putToFile(fileName: String) = File(fileName).writeText(toString())

    operator fun get(cell: Cell) = board[cell.row][cell.col]

    operator fun set(cell: Cell, c: Char) {
        board[cell.row][cell.col] = c
    }

    abstract fun clone(): Figure
}
