package generator.ranging
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
 */
import kotlin.text.*

class DifferentStringLengthException(public val message : String) : Exception(message)
enum class PatternType { PROGRESS, REPEATS, PALINDROME, WORD }

data class Pattern(public val length: Int, public val type: PatternType, public val str: String)


fun getCost(p: PatternType) = when(p) {
    PatternType.PROGRESS -> 1
    PatternType.REPEATS -> 3
    PatternType.PALINDROME -> 6
    PatternType.WORD -> 7
}

fun searcherGen(p: PatternType) = when(p) {
        PatternType.PROGRESS -> fun (s: String): List<Pattern> {

        }
        PatternType.PALINDROME -> fun (s: String): List<Pattern> {
//            s.findAll("/((.)(?1)\\2|.?)/".toRegex())
        }
        PatternType.REPEATS -> fun (s: String): List<Pattern> { // "((.)\\2+)"

        }
        PatternType.WORD -> fun (s: String): List<Pattern> {

        }
    }

/**
 *  хорошесть также должна зависеть от длины паттерна,
 *  также, паттерны могу накладываться:
 *  ABCDEFED -> 2 паттерна: ABCDEF и DEFED
 *  минимальная длина паттерна - 2 символа, максимальная - n
 */

fun getRank(s: String) = PatternType.values().flatMap { searcherGen(it)(s) }.sumBy { getCost(it.type) }

class Goodness(public val length : Int, public val rank : Int) {
    constructor(val s: String) : this(s.length, getRank(s)) {}

    fun compareTo(val g : Goodness): Int {
        if (length != g.length) throw DifferentStringLengthException()
        return rank - g.rank
    }
    // a < b ==> a.compareTo(b) < 0
}

