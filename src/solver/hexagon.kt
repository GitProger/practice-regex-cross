package solver.hexagon

import solver.row.*
import kotlin.math.*
import java.io.File
import java.lang.StringBuilder
import java.util.*

open class Hexagon(private val size: Int) {
    var regexps = MutableList(3) { MutableList(size * 2 - 1) { Row() } }
    var board = MutableList(size * 2 - 1) { i ->
        MutableList(minOf(size + i, 3 * size - 2 - i)) { '?' }
    }

    private fun getLine(dir: Dir, cell: Cell): String {
        val row = transpose(dir, cell).row
        val transposedIndices = board[row].indices
        val indices = transposedIndices.map { transposeInverse(dir, Cell(row, it)) }
        return indices.map { board[it.row][it.col] }.joinToString("")
    }

    fun generateBoard(chars: List<Char>) {
        board.forEach { row -> row.indices.forEach { row[it] = chars.random() } }
        // simulated annealing
        for (temperature in MAX_TEMPERATURE downTo 1) {
            val cell = board.indices.random().let { row -> Cell(row, board[row].indices.random()) }
            val prevChar = board[cell.row][cell.col]
            val prevCost = Dir.values().sumOf { estimateCost(getLine(it, cell)) }
            board[cell.row][cell.col] = chars.random()
            val curCost = Dir.values().sumOf { estimateCost(getLine(it, cell)) }
            if (curCost > prevCost && exp(-(curCost - prevCost).toDouble() / temperature) < Math.random()) {
                // revert changes
                board[cell.row][cell.col] = prevChar
            }
        }
    }

    enum class Dir { RIGHT, LEFT_DOWN, LEFT_UP }
    data class Cell(var row: Int = 0, var col: Int = 0)

    private fun rowSize(row: Int) = board[row].size

    private fun transposeInverse(dir: Dir, cell: Cell) = transpose(
        when (dir) {
            Dir.RIGHT -> Dir.RIGHT
            Dir.LEFT_UP -> Dir.LEFT_DOWN
            Dir.LEFT_DOWN -> Dir.LEFT_UP
        }, cell
    )

    fun solve() {
        var progress = true
        while (progress) {
            progress = false
            for (i in 0 until board.size) {
                for (j in 0 until rowSize(i)) {
                    progress = progress or process(Cell(i, j))
                }
            }
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
        for (i in Dir.values().indices)
            for (j in 0 until regexps[i].size) {
                val regex = lines[curStr++]
                regexps[i][j] = Row.fromRegex(regex, board[j].size)
            }
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

    fun putToFile(fileName: String) = File(fileName).writeText(toString())
}
