package generator

import kotlin.math.*;

fun letters(regex: String): Int {
    var r = regex.count { it in 'A'..'Z' }
    for (i in 1 until regex.length) 
        if (regex[i - 1] <= regex[i]) r++
    return r
}

fun encourage(level: Int, key: Int): Int { // we should ban to many same tokens
    return if (level < key) level else if (level < 11 * key / 10 + 2) key else 2 * key - level 
}

fun tokens(regex: String): Int {
//    var r = regex.count { it !in 'A'..'Z' }
    fun br(re: String): Int { // = re.count { it == '(' || it == ')' }
        if (re.isEmpty()) return 0
        val b = MutableList<Int>(re.length) { if (re[0] == '(') 1 else 0 }
        for (i in 1 until re.length) {
            if (re[i] == '(') b[i] = b[i - 1] + 1
            if (re[i - 1] == ')') b[i] = b[i - 1] - 1
        }
        return (0 until re.length).sumOf { if (re[it] == '(' || re[it] == ')') b[it] else 0 }
    }
    var r = 0
    r += encourage(regex.count { it == '\\' }, 2 * regex.length / 3)
    r += encourage(regex.count { it == '[' || it == ']' }, 0)
    r += 2 * regex.count { it == '?' }
    r += 2 * regex.count { it == '|' }
    r += br(regex)
    r += regex.count { it == '*' || it == '+' || it == '.' }
    return r
}

class RegexCost(public val regex: String) {
    public var complexity = tokens(regex)
    public var beauty = letters(regex)
    fun estimate() = (2.0 * sqrt(complexity.toDouble()) + 1.5 * sqrt(beauty.toDouble())).toInt()
}

fun estimateCostRegex(regex: String) = RegexCost(regex).estimate()
//fun estimateCostRegex(regex: String) = regex.count { it !in 'A'..'Z' }
