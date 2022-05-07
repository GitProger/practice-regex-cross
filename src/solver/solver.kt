package solver

import solver.row.Row
import solver.row.char
import solver.row.setAll
import java.util.*

fun hasSolution(figure: Figure) = try {
    Solver(figure).solve()
} catch(e: java.lang.IllegalStateException) {
    false // something went wrong, probably the regexps in [figure] are too difficult to parse
}

class Solver(private val figure: Figure) {
    private val uncertainCells: MutableList<Figure.Cell> = findUncertainCells()
    private val rowRegexps: List<List<Row>> = figure.regexps.mapIndexed { dir, list ->
        list.mapIndexed { idx, regex -> Row.parseFromRegex(regex, figure.rowSize(figure.directions[dir], idx)) }
    }

    private fun findUncertainCells(): MutableList<Figure.Cell> {
        val uncertainCells = mutableListOf<Figure.Cell>()
        for (i in figure.board.indices) {
            for (j in figure.board[i].indices) {
                if (figure.board[i][j] == '?') uncertainCells.add(Figure.Cell(i, j))
            }
        }
        uncertainCells.shuffle()
        return uncertainCells
    }

    fun solve(): Boolean {
        var progress = true
        while (uncertainCells.isNotEmpty() && progress) {
            progress = false
            for (cell in uncertainCells.shuffled()) {
                progress = progress or process(cell)
            }
        }
        return uncertainCells.isEmpty()
    }

    /**
     * Filters the regexps that contain the cell.
     * If the cell char is unique, it is set on the board and the cell is removed from uncertainCells
     */
    private fun process(cell: Figure.Cell): Boolean {
        var progress = false
        val result = BitSet()
        result.setAll()
        for (i in figure.directions.indices) {
            val pos = figure.transpose(figure.directions[i], cell)
            val charOr = rowRegexps[i][pos.row].charOr(pos.col)
            result.and(charOr)
        }
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
}