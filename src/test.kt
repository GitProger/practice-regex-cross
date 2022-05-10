import generator.createBoard
import generator.createRegexps
import solver.Hexagon
import solver.Rectangle
import solver.Solver
import solver.hasSolution

fun main() {
    println("Do you want to solve any crossword or to create one?")
    print("s - solve, g - generate >")
    when (readLine()!!.trim()) {
        "s" -> solve()
        "g" -> generate()
    }
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
        val times = 100 // to calculate average
        val difficulty = List(times) {
            Solver(figure).run {
                clear()
                solve()
                iterations
            }
        }.average()
        println(figure)
        println(figure.regexps.joinToString("\n\n") { it.joinToString("\n") })
        println("Approximate difficulty is $difficulty")
    } else if (type == "h") {
        print("Enter crossword size: ")
        val size = readLine()!!.toInt()
        val figure = Hexagon(size)
        createBoard(figure)
        createRegexps(figure)
        val times = 100 // to calculate average
        val difficulty = List(times) {
            Solver(figure).run {
                clear()
                solve()
                iterations
            }
        }.average()
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

fun List<Int>.average() = sumOf { it } * 1.0 / size
