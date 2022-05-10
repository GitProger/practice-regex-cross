import generator.createBoard
import generator.createRegexps
import solver.*

fun main() {
    search()
    return
    println("Do you want to solve any crossword or to create one?")
    print("s - solve, g - generate >")
    when (readLine()!!.trim()) {
        "s" -> solve()
        "g" -> generate()
    }
}

fun search() {
    var maxFigure: Figure? = null
    var maxDifficulty = 0.0
    var minFigure: Figure? = null
    var minDifficulty = 1000.0
    repeat(3) {
        val figure = Hexagon(8)
        createBoard(figure)
        createRegexps(figure)
        val difficulty = Solver(figure).difficulty()
        if (difficulty > maxDifficulty) {
            maxDifficulty = difficulty
            maxFigure = figure.clone()
        }
        if (difficulty < minDifficulty) {
            minDifficulty = difficulty
            minFigure = figure.clone()
        }
//        println(figure)
//        println(figure.regexps.joinToString("\n\n") { it.joinToString("\n") })
//        println("Approximate difficulty is $difficulty")
    }

    println("Minimum difficulty is $minDifficulty")
    Solver(minFigure!!).solve()
    println(minFigure)
    println(minFigure!!.regexps.joinToString("\n\n") { it.joinToString("\n") })
    println()
    println()
    println("Maximum difficulty is $maxDifficulty")
    Solver(maxFigure!!).solve()
    println(maxFigure)
    println(maxFigure!!.regexps.joinToString("\n\n") { it.joinToString("\n") })
}

fun generate() {
    print("r - rect, h - hex >")
    val type = readLine()!!
    if (type == "r") {
        print("Enter crossword height and width: ")
        val (h, w) = readLine()!!.split(' ').map { it.toInt() }
        val figure = Rectangle(h, w)
        createBoard(figure)
        createRegexps(figure)
        val difficulty = Solver(figure).difficulty()
        println(figure)
        println(figure.regexps.joinToString("\n\n") { it.joinToString("\n") })
        println("Approximate difficulty is $difficulty")
    } else if (type == "h") {
        print("Enter crossword size: ")
        val size = readLine()!!.toInt()
        val figure = Hexagon(size)
        createBoard(figure)
        createRegexps(figure)
        val difficulty = Solver(figure).difficulty()
        println(figure)
        println(figure.regexps.joinToString("\n\n") { it.joinToString("\n") })
        println("Approximate difficulty is $difficulty")
    }
}

fun solve() {
    print("r - rect, h - hex >")
    val type = readLine()!!
    if (type == "r") {
        print("Enter size (A)x(B) -> ")
        val (h, w) = readLine()!!.split(" ").map { it.toInt() }
        val figure = Rectangle(h, w)
        figure.readFromFile("src/solver/sample_rect.txt")
        if (hasSolution(figure)) println("Success") else println("Fail")
        println(figure)
    } else if (type == "h") {
        print("Enter size -> ")
        val s = readLine()!!.toInt()
        val figure = Hexagon(s)
        figure.readFromFile("src/solver/sample_hex.txt")
        if (hasSolution(figure)) println("Success") else println("Fail")
        println(figure)
    }
}
