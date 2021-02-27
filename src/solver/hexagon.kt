package solver.hexagon

import kotlin.math.*
import java.io.File
import java.io.BufferedReader
import java.io.InputStream

import solver.row.*

//#include "row.h"

open class Hexagon {
    var size = 0;
    var regexps = mutableListOf<MutableList<Row>>()
    var board = mutableListOf<MutableList<Char>>()

    enum class Dir { right, left_up, left_down }
    data class Cell(var row: Int = 0, var col: Int = 0)
    fun rowSize(row: Int) = board[row].size

    fun solve() {
        var progress = true
        while (progress) {
            progress = false
            for (i in 0 until board.size)
                for (j in 0 until rowSize(i))
                    progress = progress || process(Cell(i, j))
        }
    }

    fun process(cell: Cell): Boolean {
        var progress = false
        char_set result // !!!
        result.set() // !!!
        for (dir in listOf(Dir.right, Dir.left_up, Dir.left_down)) {
            pos = transpose(dir, cell)
            result = result && regexps[dir][pos.row].char_or(pos.col) // !!!
        }

        for (dir in listOf(Dir.right, Dir.left_up, Dir.left_down)) {
            pos = transpose(dir, cell)
            progress = progess || 
                regexps[dir][pos.row].set_chars(result, pos.col) // !!!
        }
        if (result.count() == 1) 
            board[cell.row][cell.col] = 'A' + result._Find_first() // !!!
        return progress
    }

    fun transpose(dir: Dir, cell: Cell) = when (dir) {
        Dir.right -> cell
        Dir.left_up -> {
            val i = cell.col + max(size - 1 - cell.row, 0)
            val j = rowSize(i) - 1 - cell.row + max(size - 1 - i, 0)
            Cell(i, j)
        }
        Dir.left_down -> {
            val i = 2 * size - 2 - (cell.col + max(cell.row - size + 1, 0))
            val j = cell.row - max(size - 1 - i, 0)
            Cell(i, j)
        }
    }

    constructor (sz: Int) {
        size = sz
        board = MutableList<MutableList<Char>>(size * 2 - 1) { 
            mutableListOf<Char>() 
        }
        regexps = MutableList<MutableList<Row>>(3) { 
            MutableList<Row>(size * 2 - 1) { Row() }
        }

        for (i in 0 until size - 1)
            board[i] = MutableList<Char>(size + i) {'?'}
        for (i in size - 1 downTo 0)
            board[size * 2 - 2 - i] = MutableList<Char>(size + i) {'?'}
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
                regexps[i][j] = Row.fromRegex(regex, board[j].size) // !!!
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
