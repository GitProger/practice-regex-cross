package solver

import java.io.File

import solver.row.*
import java.util.*

abstract class Figure {
    abstract val directions: List<String>

    data class Cell(var row: Int = 0, var col: Int = 0)

    abstract var regexps: MutableList<MutableList<Row>>
    abstract var board: MutableList<MutableList<Char>>

    fun rowSize(row: Int) = board[row].size

    abstract fun setLine(cell: Cell, dir: String, line: String)
    abstract fun getLine(cell: Cell, dir: String): String
    abstract fun getLines(dir: String): List<String>

    private val uncertainCells = mutableListOf<Cell>()

    fun fillRandomly(chars: List<Char>) {
        for (cell in uncertainCells) {
            this[cell] = chars.random()
        }
    }

    fun setLines(dir: String, lines: List<Pair<Int, String>>) {
        hide()
        val questions = getLines(dir)
        for ((i, line) in lines) {
            if (questions[i].length == line.length) setLine(transposeInverse(dir, Cell(i, 0)), dir, line)
        }
        uncertainCells.removeAll { this[it] != '?' }
    }

    fun randomCell() = uncertainCells.random()

    private fun hide() {
        uncertainCells.clear()
        for (i in board.indices) {
            for (j in board[i].indices) {
                board[i][j] = '?'
                uncertainCells.add(Cell(i, j))
            }
        }
    }

    /**
     * Solves the crossword
     */
    fun solve() {
        hide()
        var progress = true
        while (progress) {
            progress = false
            for (cell in uncertainCells) {
                progress = process(cell) || progress
            }
        }
    }

    /**
     * Filters the regexps that contain the cell.
     * If the cell char is unique, it is set on the board and the cell is removed from uncertainCells
     */
    private fun process(cell: Cell): Boolean {
        var progress = false
        val result = BitSet()
        result.setAll()
        for (i in directions.indices) {
            val pos = transpose(directions[i], cell)
            val charOr = regexps[i][pos.row].charOr(pos.col)
            result.and(charOr)
        }
        if (result.cardinality() == 0) {
            throw IllegalStateException(
                """The crossword doesn't have any solution.
This position has been determined so far:
$this
Letter at row ${cell.row} and column ${cell.col} can't be found."""
            )
        }
        for (i in directions.indices) {
            val pos = transpose(directions[i], cell)
            progress = progress or regexps[i][pos.row].setChars(result, pos.col)
        }
        if (result.cardinality() == 1) {
            this[cell] = char(result.nextSetBit(0))
            uncertainCells.remove(cell)
        }
        return progress
    }

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
                regexps[i][j] = Row.fromRegex(regex, board[j].size)
            }
    }

    fun putToFile(fileName: String) = File(fileName).writeText(toString())

    operator fun get(cell: Cell) = board[cell.row][cell.col]

    operator fun set(cell: Cell, c: Char) {
        board[cell.row][cell.col] = c
    }
}

/*
class PseudoEnum(public val vals: List<String>, public val value: String) {
    val ordinal = vals.indexOf(value)
    fun key(k: String) = PseudoEnum(vals, k)
    fun values() = vals.map { key(it) }
}
*/