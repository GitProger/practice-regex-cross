import solver.hexagon.*
import solver.rectangle.*
import solver.row.*
import java.io.File

@ExperimentalUnsignedTypes
fun main() {
	print("Enter size (A)x(B) -> ")
	val (h, w) = readLine()!!.split(" ").map { it.toInt() }
    val fig = Rectangle(h, w)
    fig.readFromFile("sample_rect.txt")
    fig.solve()
    println(fig)
}
