package solver

import java.io.File

import solver.row.*
import java.util.*

abstract class BaseFigure {
    abstract val directions: List<String>

    data class Cell(var row: Int = 0, var col: Int = 0)

    abstract var regexps: MutableList<MutableList<Row>>
    abstract var board: MutableList<MutableList<Char>>

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
        if (result.cardinality() == 1)
            board[cell.row][cell.col] = char(result.nextSetBit(0))
        return progress
    }

    protected abstract fun transpose(dir: String, cell: Cell): Cell

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
}

/*
class PseudoEnum(public val vals: List<String>, public val value: String) {
    val ordinal = vals.indexOf(value)
    fun key(k: String) = PseudoEnum(vals, k)
    fun values() = vals.map { key(it) }
}
*/