import solver.hexagon.*
import java.io.File

fun main() {
    Hexagon(7)
        .also { it.readFromFile("sample.txt") }
        .also { it.solve() }
        .also { it.putToFile("result.txt") }
    println(File("result.txt").readText())
}
