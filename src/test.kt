import generator.createBoard
import generator.createRegexps
import solver.*

fun main() {
    println("Do you want to solve any crossword or to create one?")
    print("s - solve, g - generate, e - explore (generate many and choose the most and the less difficult) > ")
    when (readLine()!!.trim()) {
        "s" -> solve()
        "g" -> generate()
        "e" -> explore()
    }
}

fun explore() {
    print("r - rect, h - hex >")
    val sample = when (readLine()!!) {
        "r" -> {
            print("Enter crossword height and width: ")
            val (h, w) = readLine()!!.split(' ').map { it.toInt() }
            Rectangle(h, w)
        }
        "h" -> {
            print("Enter crossword size: ")
            val size = readLine()!!.toInt()
            Hexagon(size)
        }
        else -> return
    }

    var maxFigure: Figure? = null
    var maxDifficulty = 0.0
    var minFigure: Figure? = null
    var minDifficulty = Double.MAX_VALUE
    print("How many figures do you want to try? ")
    repeat(readLine()!!.toInt()) {
        val figure = sample.clone()
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
    val figure: Figure = when (type) {
        "r" -> {
            print("Enter crossword height and width: ")
            val (h, w) = readLine()!!.split(' ').map { it.toInt() }
            Rectangle(h, w)
        }
        "h" -> {
            print("Enter crossword size: ")
            val size = readLine()!!.toInt()
            Hexagon(size)
        }
        else -> return
    }
    createBoard(figure)
    createRegexps(figure)
    val difficulty = Solver(figure).difficulty()
    println(figure)
    println(figure.regexps.joinToString("\n\n") { it.joinToString("\n") })
    println("Approximate difficulty is $difficulty")
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
        println("The approximate difficulty is ${Solver(figure).difficulty()}")
        println("The approximate difficulty with human factor is ${Solver(figure).difficulty(true)}")
    }
}
