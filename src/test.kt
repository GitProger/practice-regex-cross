import generator.prepareBoard
import generator.ranging.PatternType
import generator.ranging.cost
import solver.hexagon.*
import solver.rectangle.*

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
        val r = Rectangle(h, w)
        prepareBoard(r)
        println(r)
    } else if (type == "h") {
        print("Enter crossword size: ")
        val size = readLine()!!.toInt()
        val h = Hexagon(size)
        prepareBoard(h)
        for (pattern in PatternType.values()) {
            println(pattern)
            for (dir in h.directions) {
                println(dir)
                for (row in h.getLines(dir)) {
                    var ans = 0
                    var best = 0..-1
                    for (i in row.indices) {
                        for (j in i + 2 until row.length) {
                            if (ans < cost(row.substring(i..j), pattern)) {
                                ans = cost(row.substring(i..j), pattern)
                                best = i..j
                            }
                        }
                    }
                    println(row.mapIndexed { index, char -> if (index in best) char else char - ('A' - 'a') }.joinToString(""))
                }
            }
        }
        println(h)
    }
}

fun solve() {
    print("r - rect, h - hex >")
    val type = readLine()!!
    if (type == "r") {
        print("Enter size (A)x(B) -> ")
        val (h, w) = readLine()!!.split(" ").map { it.toInt() }
        val fig = Rectangle(h, w)
        fig.readFromFile("src/solver/sample_rect.txt")
        fig.solve()
        println(fig)
    } else if (type == "h") {
        print("Enter size -> ")
        val s = readLine()!!.toInt()
        val fig = Hexagon(s)
        fig.readFromFile("src/solver/sample_hex.txt")
        fig.solve()
        println(fig)
    }
}