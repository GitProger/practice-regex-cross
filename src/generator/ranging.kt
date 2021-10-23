package generator.ranging

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
        3 * pattern.length.pow(1.5)
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
        2 * count.pow(1.5)
    }
    PatternType.PALINDROME -> {
        3 * pattern.length.pow(1.5)
    }
    PatternType.WORD -> {
		pattern.length.pow(2.0)
	}
}

/**
 *  хорошесть также должна зависеть от длины паттерна,
 *  также, паттерны могу накладываться:
 *  ABCDEFED -> 2 паттерна: ABCDEF и DEFED
 *  минимальная длина паттерна - 2 символа, максимальная - n
 */

fun isProgression(s: String): Boolean {
    // ABC AXBXCX XAXBXC  CBA - no
    val ok = MutableList(2) { true }
    for (shift in 1 .. 2) {
        var prev = 'A'
        for (i in 0 until s.length step shift) {
            if (i > 0 && s[i] - prev != 1)
                ok[shift - 1] = false
            prev = s[i]
        }
    }
    return ok[0] || ok[1]
}

fun isPalindrome(s: String): Boolean {
    val n = s.length
    val mid1 = s.subSequence(0, n / 2)
    val mid2 = s.subSequence(n - n / 2, n)
    return mid1 == mid2.reversed()
}

fun doesRepeat(s: String): Boolean {
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

fun corresponds(pattern: String, p: PatternType) = when (p) {
    PatternType.PROGRESS -> {
        isProgression(pattern)
    }
    PatternType.PALINDROME -> {
        isPalindrome(pattern)
    }
    PatternType.REPEATS -> {
        doesRepeat(pattern)
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
