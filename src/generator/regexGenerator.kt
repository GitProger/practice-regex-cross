package generator

import solver.hasSolution
import solver.Figure
import java.lang.Math.random
import kotlin.math.*

private const val MAX_TEMPERATURE = 10000
private const val Q: Double = 1e3

fun createRegexps(f: Figure) {
//    val rowRegexps = f.directions.map {
//        f.getLines(it).map { line -> Row.fromRegex(line, line.length) }.toTypedArray()
//    }
    // todo: transformation stringRegex -> rowRegex could be optimized with memoization as in the comment above
    for (dir in f.directions.indices) {
        for ((index, line) in f.getLines(f.directions[dir]).withIndex()) {
            f.regexps[dir][index] = line
        }
    }
    // simulated annealing
    for (temperature in MAX_TEMPERATURE downTo 1) {
        val dir = f.directions.indices.random()
        val index = f.regexps[dir].indices.random()
        val newRegexp = generateRegexp(f.getLines(f.directions[dir])[index])
        val oldRegexp = f.regexps[dir][index]
        val oldCost = estimateCostRegex(oldRegexp)
        val newCost = estimateCostRegex(newRegexp)
        f.regexps[dir][index] = newRegexp
        val probability = exp(-(oldCost - newCost) * Q / temperature)
        if ((oldCost > newCost && probability < random()) || !hasSolution(f)) {
            // revert changes
            f.regexps[dir][index] = oldRegexp
            continue
        }
        // todo: draw plot of changing cost
        // todo: experiment wih different probability functions based on the plot
        println("Changes accepted")
    }
}

fun findPeriod(s: String): Int? {
    for (len in s.length - 1 downTo 1) {
        if (s.length % len != 0) continue
        if ((len until s.length).all { i -> s[i] == s[i - len] }) return len
    }
    return null
}

private fun String.shuffled() = toList().shuffled().joinToString("")

fun encodeChar(c: Char, other: String): String {
    if (random() < 0.3) return "."
    val count = listOf(1, 2, 1, 3, 1, 2, 1).random()
    var s = "$c"
    repeat(count - 1) {
        val random = other.random()
        if (random !in s) s += random
    }
    return if (s.length == 1) s else "[${s.shuffled()}]"
}

fun generateRegexp(s: String): String {
    // todo: improve the function
    fun generate(s: String): String {
        val period = findPeriod(s)
        if (period != null) {
            val r = generate(s.substring(0 until period))
            if (r.length == 1 || r.enclosedInBrackets()) return r + "*+".random()
            if ('(' !in r) return "($r)" + "*+".random()
        }
        if (s.length > 2 && isPalindrome(s)) {
            return s.lowercase()
        }
        return s.map { encodeChar(it, s.filter { it.isUpperCase() }) }.joinToString("")
    }

    var regex = ""
    var cur = ""
    for (char in s) {
        cur += char
        if (random() < 0.1) {
            regex += generate(cur)
            cur = ""
        }
    }
    if (cur.isNotEmpty()) regex += generate(cur)
    val indexOfParentheses = HashMap<Char, Int>()
    var currentIndex = 0
    var final = ""
    for (i in regex.indices) {
        if (regex[i] == '(') currentIndex++
        if (!regex[i].isLowerCase()) {
            final += regex[i]
            continue
        }
        if (indexOfParentheses.containsKey(regex[i])) {
            final += "\\${indexOfParentheses[regex[i]]}"
        } else {
            if (regex.count { it == regex[i] } > 1) {
                final += "(.)"
                indexOfParentheses[regex[i]] = ++currentIndex
            } else {
                final += "${regex[i].uppercaseChar()}"
            }
        }
    }

    return final
}

private fun String.enclosedInBrackets(): Boolean {
    if (isEmpty()) return false
    return this[0] == '[' && indices.find { this[it] == ']' } == lastIndex
}
