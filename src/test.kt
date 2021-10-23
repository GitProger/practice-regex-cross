import generator.generateBoard
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
        generateBoard(r, ('A'..'Z').toList())
        r.putToFile("src/solver/result.txt")
        println(r)
    } else if (type == "h") {
        print("Enter crossword size: ")
        val size = readLine()!!.toInt()
        val h = Hexagon(size)
        generateBoard(h, ('A'..'Z').toList())
        h.putToFile("src/solver/result.txt")
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