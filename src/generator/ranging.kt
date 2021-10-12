package generator.ranging

import java.io.File

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

fun getCost(p: PatternType) = when (p) {
    PatternType.PROGRESS -> 1
    PatternType.REPEATS -> 3
    PatternType.PALINDROME -> 6
    PatternType.WORD -> 7
}

/**
 *  хорошесть также должна зависеть от длины паттерна,
 *  также, паттерны могу накладываться:
 *  ABCDEFED -> 2 паттерна: ABCDEF и DEFED
 *  минимальная длина паттерна - 2 символа, максимальная - n
 */

fun isProgression(s: String): Boolean = false

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
    return pattern.length * getCost(p)
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
