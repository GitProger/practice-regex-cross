package generator

import solver.Figure
import kotlin.math.exp

private const val MAX_TEMPERATURE = 10000
private const val Q: Double = 1e3

fun setLines(figure: Figure, dir: String, lines: List<Pair<Int, String>>) {
    for (i in 0 until figure.board.size) {
        for (j in 0 until figure.board[i].size) figure.board[i][j] = '?'
    }
    val questions = figure.getLines(dir)
    for (i in questions.indices) {
        figure.setLine(figure.transposeInverse(dir, Figure.Cell(i, 0)), dir, questions[i].replace(".".toRegex(), "?"))
    }
    for ((i, line) in lines) {
        if (questions[i].length == line.length) {
            figure.setLine(figure.transposeInverse(dir, Figure.Cell(i, 0)), dir, line)
        }
    }
}

fun createBoard(figure: Figure) {
    val dir = figure.directions[0]
    // todo: not a very prioritized one, but hard-coded "GREETINGS" should be replaced with something else
    // todo: also one should handle exception which is thrown when figure.rowSize(dir, 0) > "GREETINGS".length
    setLines(figure, dir, listOf(0 to "GREETINGS".substring(0 until figure.rowSize(dir, 0))))
    val chars = ('A'..'Z').toList()
    generateBoard(figure, chars)
}

private fun generateBoard(figure: Figure, chars: List<Char>) {
    val space = mutableListOf<Figure.Cell>()
    for (i in 0 until figure.board.size) {
        for (j in 0 until figure.board[i].size) {
            if (figure.board[i][j] == '?') {
                figure.board[i][j] = chars.random()
                space.add(Figure.Cell(i, j))
            }
        }
    }
    // simulated annealing
    val f = FigureCostKeeper(figure)
    for (temperature in MAX_TEMPERATURE downTo 1) {
        val cell = space.random()
        val prevChar = f[cell]
        val oldCost = f.cost()
        f[cell] = chars.random()
        val newCost = f.cost()

        val probability = exp(-(oldCost - newCost) * Q / temperature)
        if (oldCost != newCost) {
            println("$temperature: oldCost is $oldCost, newCost is $newCost")
        }
        // todo: draw plot of changing cost;
        // todo: experiment wih different probability functions based on the plot
        if (oldCost < newCost || probability > Math.random()) {
            println("Changes accepted")
            println(f.cost())
            continue
        }
        // revert changes
        f[cell] = prevChar
    }
}