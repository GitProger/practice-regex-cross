import solver.hexagon.*
import solver.rectangle.*
import java.io.File

// kotlinc mainSolver.kt row.kt hexagon.kt rectangle.kt -include-runtime -d main.jar

@ExperimentalUnsignedTypes
fun main() {
    val fig = Rectangle(2, 3) // Hexagon(7)
    fig.readFromFile("sample_rect.txt")
    fig.solve()
    fig.putToFile("result.txt")
    println(File("result.txt").readText())
}
