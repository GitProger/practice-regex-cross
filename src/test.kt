import generator.generateBoard
import solver.Hexagon
import solver.Rectangle

fun main() {
    println("Do you want to solve any crossword or to create one?")
    print("s - solve, g - generate >")
    when ("g") {
        "s" -> solve()
        "g" -> generate()
    }
}

fun generate(){
    print("r - rect, h - hex >")
    if (false) {
        TODO("Not implemented yet")
    } else {
        print("Enter crossword size: ")
        val size = 5
        val h = Hexagon(size)
        generateBoard(h, "ABCDEFGHRT".toList())
        h.putToFile("src/solver/result.txt")
        println()
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
