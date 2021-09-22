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

object PatternRegexes {
    val progress = "-" // ?
    val repeats = "((.)\\2+)"
    val palindrome = "/((.)(?1)\\2|.?)/"
    val word = "-" // ?
}

object PatternCost { // насколько приятно увидеть в строке что-то из этого
    val progress = 1
    val repeats = 3
    val palindrome = 6
    val word = 7
}

enum class PatternType { PROGRESS, REPEATS, PALINDROME, WORD }

data class Pattern(public val length: Int, public val type: PatternType)


/**
 *  хорошесть также должна зависеть от длины паттерна,
 *  также, паттерны могу накладываться:
 *  ABCDEFED -> 2 паттерна: ABCDEF и DEFED
 *  минимальная длина паттерна - 2 символа, максимальная - n
 */

class PatternSearcher(val s: String) {
    val patterns = mutableListOf<Pattern>()

}

class Goodness(public val length : Int, public val rank : Int) {
    constructor(val s: String) : this(s.length, PatternSearcher(s).all()) {}

    fun compareTo(val g : Goodness): Int {
        if (length != g.length) throw DifferentStringLengthException()
        return rank - g.rank
    }
    // a < b ==> a.compareTo(b) < 0
}
