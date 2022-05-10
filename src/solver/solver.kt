package solver

import solver.row.Row
import solver.row.char
import solver.row.setAll
import java.util.*

fun hasSolution(figure: Figure) = Solver(figure).solve()

class Solver(private val figure: Figure) {
    private var uncertainCells: MutableList<Figure.Cell> = findUncertainCells()
    private var rowRegexps: List<List<Row>> = figure.regexps.mapIndexed { dir, list ->
        list.mapIndexed { idx, regex -> Row.parseFromRegex(regex, figure.rowSize(figure.directions[dir], idx)) }
    }

    private fun findUncertainCells(): MutableList<Figure.Cell> {
        val uncertainCells = mutableListOf<Figure.Cell>()
        for (i in figure.board.indices) {
            for (j in figure.board[i].indices) {
                if (figure.board[i][j] == '?') uncertainCells.add(figure.Cell(i, j))
            }
        }
        uncertainCells.shuffle()
        return uncertainCells
    }

    fun clear() {
        uncertainCells = mutableListOf<Figure.Cell>()
        for (i in figure.board.indices) {
            for (j in figure.board[i].indices) {
                uncertainCells.add(figure.Cell(i, j))
                figure.board[i][j] = '?'
            }
        }
        uncertainCells.shuffle()
        rowRegexps = figure.regexps.mapIndexed { dir, list ->
            list.mapIndexed { idx, regex -> Row.parseFromRegex(regex, figure.rowSize(figure.directions[dir], idx)) }
        }
    }

    var iterations = 0

    fun solve(humanFactor: Boolean = false): Boolean {
        if (uncertainCells.isEmpty()) return true
        iterations = 0
        val useful = BooleanArray(uncertainCells.maxOf { it.code() } + 1) { false }
        for (cell in uncertainCells) useful[cell.code()] = true
        try {
            while (uncertainCells.isNotEmpty() && useful.any { it }) {
                val cell = uncertainCells.random()
                if (process(cell, humanFactor)) {
                    useful.fill(false)
                    for (c in uncertainCells) useful[c.code()] = true
                } else {
                    useful[cell.code()] = false
                }
                iterations++
            }
        } catch (e: java.lang.IllegalStateException) {
            return false
            // something went wrong, probably the regexps in [figure] are too difficult to parse
            // or the crossword doesn't any have solution
        }
        return uncertainCells.isEmpty()
    }

    /**
     * Filters the regexps that contain the cell.
     * If the cell char is unique, it is set on the board and the cell is removed from uncertainCells
     */
    private fun process(cell: Figure.Cell, humanFactor: Boolean): Boolean {
        var progress = false
        val result = BitSet()
        result.setAll()
        for (i in figure.directions.indices) {
            val pos = figure.transpose(figure.directions[i], cell)
            val charOr = rowRegexps[i][pos.row].charOr(pos.col)
            result.and(charOr)
        }
        if (humanFactor && result.cardinality() >= 2) return false
        if (result.cardinality() == 0) {
            throw IllegalStateException(
                """The crossword doesn't have any solution.
This position has been determined so far:
$figure
Letter at row ${cell.row} and column ${cell.col} can't be found."""
            )
        }
        for (i in figure.directions.indices) {
            val pos = figure.transpose(figure.directions[i], cell)
            progress = progress or rowRegexps[i][pos.row].setChars(result, pos.col)
        }
        if (result.cardinality() == 1) {
            figure[cell] = char(result.nextSetBit(0))
            uncertainCells.remove(cell)
        }
        return progress
    }

    fun difficulty(humanFactor: Boolean = false): Double {
        var sum = 0.0
        val times = 10
        repeat(times) {
            clear()
            if (!solve(humanFactor)) return 0.0
            sum += iterations
        }
        return sum / times
    }
}
