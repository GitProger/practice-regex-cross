package solver.rectangle

import solver.row.*
import kotlin.math.*
import java.io.File
import java.lang.StringBuilder
import java.util.*

open class Rectangle(val height: Int, val width: Int) {
    var regexps = MutableList(2) {
        MutableList(if (it == 0) height else width) { Row() } 
    }
    var board = MutableList(height) { MutableList(width) { '?' } }

    private enum class Dir { RIGHT, DOWN }
    data class Cell(var row: Int = 0, var col: Int = 0)

    fun solve() {
        var progress = true
        while (progress) {
            progress = false
            for (i in 0 until height)
                for (j in 0 until width)
                    progress = process(Cell(i, j)) || progress
        }
    }

    private fun process(cell: Cell): Boolean {
        var progress = false
        val result = BitSet()
        result.setAll()
        for (dir in Dir.values()) {
            val pos = transpose(dir, cell)
            val charOr = regexps[dir.ordinal][pos.row].charOr(pos.col)
            result.and(charOr)
        }
        if (result.cardinality() == 0) {
            throw IllegalStateException("""The crossword doesn't have any solution.
This position has been determined so far:
$this
Letter at row ${cell.row} and column ${cell.col} can't be found.""")
        }
        for (dir in Dir.values()) {
            val pos = transpose(dir, cell)
            progress = progress or regexps[dir.ordinal][pos.row].setChars(result, pos.col)
        }
        if (result.cardinality() == 1)
            board[cell.row][cell.col] = char(result.nextSetBit(0))
        return progress
    }


    private fun transpose(dir: Dir, cell: Cell) = when (dir) {
        Dir.RIGHT -> cell
        Dir.DOWN -> Cell(cell.col, cell.row)
    }

    fun readFromFile(fileName: String) {
        val lines = mutableListOf<String>()
        File(fileName).inputStream().bufferedReader().forEachLine {
            if (it.isNotBlank()) lines.add(it.trim())
        }
        var curStr = 0
        for (i in 0..1)
            for (j in 0 until regexps[i].size) {
                val regex = lines[curStr++]
                regexps[i][j] = Row.fromRegex(regex, width)
            }
    }

    override fun toString(): String {
        var s = ""
        for (i in 0 until height) {
            val text = (0 until width).toList()
                     .map { board[i][it].toString() }.joinToString(" ") + "\n"
            s += text
        }
        return s
    }

    fun putToFile(fileName: String) = File(fileName).writeText(toString())
}

class PseudoEnum(public val vals: List<String>, public val value: String) {
    val ordinal = vals.indexOf(value)
    fun key(k: String) = PseudoEnum(vals, k)
    fun values() = vals.map { key(it) }
}
