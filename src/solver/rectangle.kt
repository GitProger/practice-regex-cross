package solver.rectangle

import kotlin.math.*
import java.io.File

import solver.row.*

open class Rectangle(var height: Int, var width: Int) {
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

    fun process(cell: Cell): Boolean {
        var progress = false
        var result = 1u shl alphabet - 1
        for (dir in listOf(Dir.RIGHT, Dir.DOWN)) {
            val pos = transpose(dir, cell)
            result = result and regexps[dir.ordinal][pos.row].charOr(pos.col)
        }

        for (dir in listOf(Dir.RIGHT, Dir.DOWN)) {
            val pos = transpose(dir, cell)
            progress = progress or regexps[dir.ordinal][pos.row].setChars(result, pos.col)
        }
        if (result.toString(2).count { it == '1' } == 1)
            board[cell.row][cell.col] = 'A' + result.toString(2).let { it.lastIndex - it.indexOf('1') }
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

    fun putToFile(fileName: String) {
        val result = File(fileName)
        result.writeText("")
        for (i in 0 until height) {
            val text = " " + (0 until width).toList()
                     .map { board[i][it].toString() }.joinToString(" ") + "\n"
            result.appendText(text)
        }
    }
}
