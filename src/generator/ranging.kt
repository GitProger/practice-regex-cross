package generator.ranging

import solver.Figure
import java.io.File
import kotlin.math.*

fun Int.pow(p: Double): Int {
    return this.toDouble().pow(p).toInt()
}

/**
 * Файл реализует получение "хорошести" строки
 * Что же такое "хорошесть"?
 * Мы хотим, чтобы строка была максимально интерсной,
 * то есть в ней бы встерчались бы повторы или идущие
 * подряд в алфавите буквы, палиндромы,
 * или же какие-то английские слова
 * плохая строка: HZEHIUHFABEHOIE
 * хорошая строка: ABCDIFBYYYYCATOTACOOO
 *
 * прогресии: axbxcxdx, abcd
 *
 * Но постоянные паттерны могут надоесть, поэтому будем использовать рандом
 * и оценивать только вклад самого длинного паттерна
 */

enum class PatternType { PROGRESS, REPEATS, PALINDROME, WORD }

fun getCost(p: PatternType, pattern: String) = when (p) {
    PatternType.PROGRESS -> {
        3 * pattern.length.pow(2.0)
    }
    PatternType.REPEATS -> {
        var count = 0
        val n = pattern.length
        for (periodLen in 1..n / 2) {
            val period = pattern.subSequence(0, periodLen)
            if (n % periodLen == 0 && (pattern.indices step periodLen).all {
                    pattern.subSequence(it, it + periodLen) == period
                }) {
                count = n / periodLen
                break
            }
        }
        2 * count.pow(2.0)
    }
    PatternType.PALINDROME -> {
        pattern.length.pow(2.0)
    }
    PatternType.WORD -> {
        pattern.length.pow(2.5)
    }
}

/**
 *  хорошесть также должна зависеть от длины паттерна,
 *  также, паттерны могу накладываться:
 *  ABCDEFED -> 2 паттерна: ABCDEF и DEFED
 *  минимальная длина паттерна - 2 символа, максимальная - n
 */

fun allEqual(s: String) = s.all { it == s.first() }

fun isProgression(s: String): Boolean {
    // ABC AXBXCX XAXBXC - yes
    // CBA - no
    fun isStraight(s: String): Boolean {
        for (i in 1 until s.length) {
            if (s[i] - s[i - 1] != 1) return false
        }
        return s.length > 1
    }

    val odd = s.slice(1 until s.length step 2)
    val even = s.slice(s.indices step 2)
    return isStraight(s) || (isStraight(odd) && allEqual(even)) || (isStraight(even) && allEqual(odd))
}

fun isPalindrome(s: String) = s == s.reversed()

fun doesRepeat(s: String): Boolean {
    if (isPalindrome(s)) return false
    val n = s.length
    for (periodLen in 1..n / 2) {
        val period = s.subSequence(0, periodLen)
        if (n % periodLen == 0 &&
            (s.indices step periodLen).all { s.subSequence(it, it + periodLen) == period }
        )
            return true
    }
    return false
}

val dictionary = File("src/generator/db/dict.txt").bufferedReader().readLines()

fun inDict(s: String) = s in dictionary

fun corresponds(pattern: String, p: PatternType) = !allEqual(pattern) && when (p) {
    PatternType.PROGRESS -> {
        isProgression(pattern)
    }
    PatternType.REPEATS -> {
        doesRepeat(pattern)
    }
    PatternType.PALINDROME -> {
        isPalindrome(pattern)
    }
    PatternType.WORD -> {
        inDict(pattern)
    }
}

fun cost(pattern: String, p: PatternType): Int {
    if (!corresponds(pattern, p)) return 0
    return getCost(p, pattern)
}

fun estimateCost(s: String): Int {
    val n = s.length
    val dp = MutableList<Int>(n) { 0 }
    for (i in 0 until n) {
        for (j in 0 until i) {
            for (p in PatternType.values()) {
                dp[i] = maxOf(dp[i], dp[j] + cost(s.subSequence(j, i + 1).toString(), p))
            }
        }
    }
    return dp.last()
}

fun estimateCost(s: String, pt: PatternType): Int {
    var ans = 0
    for (i in s.indices) {
        for (j in i + 2 until s.length) {
            ans = maxOf(ans, cost(s.substring(i..j), pt))
        }
    }
    return ans
}

class FigureWithCost(private val f: Figure) {
    inner class CostKeeper {
        private val costForPattern = IntArray(PatternType.values().size)
        private val maxCostForPattern = IntArray(4) { f.rowSize(0).pow(3.5) }

        init {
            for (pt in PatternType.values()) {
                for (dir in f.directions) {
                    costForPattern[pt.ordinal] += f.getLines(dir).sumOf { estimateCost(it, pt) }
                }
            }
        }

        fun joinCosts(): Int {
            val sorted = costForPattern.mapIndexed { index, cost -> minOf(cost, maxCostForPattern[index]) }.sorted()
            var ans = 0
            for (i in sorted.indices) ans += (sorted.size - i) * sorted[i]
            return ans
        }

        fun exclude(cell: Figure.Cell) {
            for (pt in PatternType.values()) {
                for (dir in f.directions) {
                    costForPattern[pt.ordinal] -= estimateCost(f.getLine(cell, dir), pt)
                }
            }
        }

        fun include(cell: Figure.Cell) {
            for (pt in PatternType.values()) {
                for (dir in f.directions) {
                    costForPattern[pt.ordinal] += estimateCost(f.getLine(cell, dir), pt)
                }
            }
        }
    }

    private val costKeeper = CostKeeper()

    operator fun set(cell: Figure.Cell, c: Char) {
        costKeeper.exclude(cell)
        f[cell] = c
        costKeeper.include(cell)
    }

    operator fun get(cell: Figure.Cell) = f[cell]

    fun cost() = costKeeper.joinCosts()
    fun randomCell() = f.randomCell()
}