import solver.hexagon.*
import solver.rectangle.*
import solver.row.*
import java.io.File

fun main() {
	val type = "h"
	if (type == "r") {
        print("Enter size (A)x(B) -> ")
        val (h, w) = readLine()!!.split(" ").map { it.toInt() }
        val fig = Rectangle(h, w)
        fig.readFromFile("sample_rect.txt")
        fig.solve()
        println(fig)
    } else if (type == "h") {
        print("Enter size -> ")
        val s = readLine()!!.toInt()
        val fig = Hexagon(s)
        fig.readFromFile("sample_hex.txt")
        fig.solve()
        println(fig)        
    }
}
