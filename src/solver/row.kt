package solver.row

import java.util.*
import kotlin.collections.HashMap

private fun Boolean.toInt() = if (this) 1 else 0
const val alphabet = 26
fun BitSet.setAll() = set(0, alphabet)
fun isLetter(c: Char) = c in 'A'..'Z'
fun ordinal(c: Char) = c - 'A'
fun char(ord: Int) = 'A' + ord

class Row() {
    class Word() {
        /**
         * The constructor supposes there are only ?[^].ABC in regex
         */
        constructor(regex: String, l: Int, r: Int, qMask: Int) : this() {
            val skip = regex.indices.filter { regex[it] == '?' }.filterIndexed { q, _ -> qMask and (1 shl q) != 0 }
            var i = l - 1
            while (++i <= r) {
                if (i + 1 <= r && regex[i + 1] == '?' && (i + 1) in skip) continue

                if (regex[i] == '?') continue
                if (regex[i] == '[') {
                    val curR = findClose(regex, i)
                    letters.add(parseFromBrackets(regex, i, curR).letters[0])
                    i = curR - 1
                    continue
                }
                letters.add(BitSet())
                when {
                    regex[i] == '.' -> letters.last().setAll()
                    isLetter(regex[i]) -> letters.last()[ordinal(regex[i])] = true
                    else -> throw IllegalStateException("Regex $regex isn't correct. Letter ${regex[i]} doesn't belong to range A..Z")
                }
            }
        }

        class SubExpr(val l: Int, val r: Int) {
            val size: Int get() = r - l + 1
        }

        val subExprs = mutableListOf<SubExpr>()

        val letters = mutableListOf<BitSet>()

        val size: Int get() = letters.size

        override fun toString(): String {
            return letters.joinToString(" ") { it.stream().toArray().map { char(it) }.joinToString("") }
        }

        fun clone(): Word {
            val clone = Word()
            for (subExpr in subExprs) clone.subExprs.add(subExpr)
            char@ for (i in 0 until size) {
                for (j in 0 until i) {
                    if (letters[i] === letters[j]) {
                        clone.letters.add(clone.letters[j])
                        continue@char
                    }
                }
                clone.letters.add(letters[i].clone() as BitSet)
            }
            return clone
        }
    }

    override fun toString() = "${words.size}\n" + words.joinToString("\n") { it.toString() }

    private val words = mutableListOf<Word>()

    fun setChars(allowed: BitSet, idx: Int): Boolean {
        var changed = false
        for (word in words) {
            val a = word.letters[idx].clone() as BitSet
            a.and(allowed)
            changed = changed || a.cardinality() < word.letters[idx].cardinality()
            word.letters[idx].and(a)
        }
        clean()
        return changed
    }

    constructor(word: Word) : this() {
        words.add(word)
    }

    fun append(row: Row): Row {
        for (word in row.words) words.add(word)
        return this
    }

    fun multiConcatenatedWith(row: Row, size: Int): Row {
        val result = Row()
        val leftCnt = words.size
        val rightCnt = row.words.size
        for (i in 0 until leftCnt) {
            for (j in 0 until rightCnt) {
                val leftSize = words[i].size
                val rightSize = row.words[j].size
                if (leftSize + rightSize != size) continue
                result.words.add(words[i].clone())
                for (letter in row.words[j].letters) {
                    result.words.last().letters.add(letter)
                }
                for (subExpr in row.words[j].subExprs) {
                    result.words.last().subExprs.add(Word.SubExpr(subExpr.l + leftSize, subExpr.r + leftSize))
                }
            }
        }
        return result
    }

    private fun clean() = words.removeAll { word -> word.letters.any { letter -> letter.isEmpty } }

    fun charOr(idx: Int) = BitSet().apply { words.onEach { word -> or(word.letters[idx]) } }

    fun distinctLengths() = List(words.size) { words[it].size }.sorted().distinct()

    companion object Getter {
        private var dp = HashMap<Pair<Int, String>, Row>()
        private var last = mutableListOf<Row?>()

        fun clearCaches() = dp.clear()

        private fun getRepChar(regex: String, i: Int) = if (isRepChar(regex[i])) regex[i] else '1'
        private fun isRepChar(c: Char) = c == '?' || c == '+' || c == '*'
        private fun isDigit(c: Char) = c in '0'..'9'

        private fun findOpeningPair(s: String, closing: Int): Int {
            if (s[closing] != ']' && s[closing] != ')') return closing - isDigit(s[closing]).toInt() // back reference
            for (i in closing downTo 0) {
                if (s[i] == if (s[closing] == ')') '(' else '[') return i
            }
            throw IllegalStateException("Regex doesn't contain pair for closing parenthesis at $closing")
        }

        private fun findClose(s: String, opening: Int): Int {
            if (s[opening] != '[' && s[opening] != '(') return opening + 1 + isDigit(s[opening]).toInt() // back reference
            for (i in opening until s.length) {
                if (s[i] == if (s[opening] == '(') ')' else ']') return i + 1
            }
            throw IllegalStateException("Regex doesn't contain pair for opening parenthesis at $opening")
        }

        /**
         * Supposes there are only ^ABC in regex and (regex[[l]] == '[' && regex[[r]] == ']')
         */
        private fun parseFromBrackets(regex: String, l: Int, r: Int): Word {
            assert(regex[l] == '[')
            val word = Word()
            word.letters.add(BitSet())
            val invert = regex[l + 1] == '^'
            if (invert) word.letters[0].setAll()
            for (i in (l + invert.toInt() + 1)..r) {
                if (regex[i] == ']') return word.also { assert(i == r) }
                if (!isLetter(regex[i])) {
                    throw IllegalStateException("Regex $regex isn't correct. Letter ${regex[i]} doesn't belong to range A..Z")
                }
                word.letters[0][ordinal(regex[i])] = !invert
            }
            throw IllegalStateException("Regex $regex doesn't contain pair for opening bracket at $l")
        }

        private fun getConcatenated(regex: String, r: Int, row: Row, required_size: Int): Row {
            val lengths = row.distinctLengths()
            val result = Row()
            for (len in lengths) {
                result.words.addAll(
                    parseFromRegex(regex, r, required_size - len)
                        .multiConcatenatedWith(row, required_size).words
                )
            }
            return result
        }

        /**
         * Creates row from [regex]. [regex] should be a correct regular expression
         *
         * -Supposes there are no +*, back references and nested parentheses in parentheses
         *
         * -Supposes there are no - in []
         *
         * -Supposes that AB..YZ and . are the only letter characters
         */
        private fun parseFromRegex(regex: String, r: Int, required_size: Int): Row {
            if (r == -1 && required_size == 0) return Row(Word())
            if (r <= -1 || required_size < 0) return Row()
            val memo = dp[required_size to regex.slice(0..r)]
            if (memo != null) return memo
            val rep = getRepChar(regex, r)
            return parseFromRegexWithRep(regex, r - isRepChar(regex[r]).toInt(), rep, required_size)
        }

        private fun parseFromRegexWithRep(regex: String, r: Int, rep: Char, required_size: Int): Row {
            val mid = findOpeningPair(regex, r)
            val result = Row()
            if (isDigit(regex[r])) { //back reference
                if (rep == '+' || rep == '*') {
                    result.append(getConcatenated(regex, r + 1, regex[r] - '0', required_size))
                } else if (rep == '1') {
                    result.append(getConcatenated(regex, mid - 1, regex[r] - '0', required_size))
                }
            } else {
                val lastExpr = getLast(regex, mid, r)
                if (rep == '+' || rep == '*') {
                    result.append(getConcatenated(regex, r + 1, lastExpr, required_size))
                }
                if (rep == '1') {
                    result.append(getConcatenated(regex, mid - 1, lastExpr, required_size))
                }
            }
            if (rep == '+' || rep == '?') {
                result.append(parseFromRegex(regex, r, required_size))
            }
            if (rep == '*' || rep == '?') {
                result.append(parseFromRegex(regex, mid - 1, required_size))
            }
            dp[required_size to regex.slice(0..r + isRepChar(rep).toInt())] = result
            return result
        }

        private fun getConcatenated(regex: String, r: Int, group: Int, required_size: Int): Row {
            fun ending(ord: Int) = when (ord) {
                1 -> "st"
                2 -> "nd"
                else -> "th"
            }

            val groupL = regex.indices.filter { regex[it] == '(' }.getOrNull(group - 1)
                ?: throw IllegalStateException("Regex $regex doesn't contain ${group}${ending(group)} group in parentheses")
            val groupR = findClose(regex, groupL)
            val temp = parseFromParentheses(regex, groupL, groupR)
            val lengths = temp.distinctLengths()
            val result = Row()
            for (len in lengths) {
                val left = parseFromRegex(regex, r, required_size - len)
                for (word in left.words) {
                    val expr = word.subExprs[group - 1]
                    if (expr.size != len) continue
                    result.words.add(word)
                    for (i in expr.l..expr.r) {
                        result.words.last().letters.add(result.words.last().letters[i])
                    }
                }
            }
            return result
        }

        /**
         * Supposes there are no nested parentheses.
         *
         * Supposes that there are only (|) and ?[^].ABC characters in regex
         */
        private fun parseFromParentheses(regex: String, l: Int, r: Int): Row {
            assert(regex[l] == '(')
            val row = Row()
            var curL = l + 1
            for (i in l + 1..r) {
                if (regex[i] == '|' || regex[i] == ')') {
                    if (regex.slice(curL until i).count { it == '?' } > 15) {
                        throw IllegalStateException("Regex $regex is too complicated")
                    }
                    for (mask in 0 until (1 shl regex.slice(curL until i).count { it == '?' })) {
                        row.words.add(Word(regex, curL, i - 1, mask))
                        row.words.last().subExprs.add(Word.SubExpr(0, row.words.last().size - 1))
                    }
                    curL = i + 1
                    if (regex[i] == ')') return row
                }
            }
            throw IllegalStateException("Regex $regex doesn't contain pair for opening parenthesis at $l")
        }

        fun parseFromRegex(regex: String, required_size: Int): Row {
            last = MutableList<Row?>(regex.length + 1) { null }
            val ans = parseFromRegex(regex, regex.lastIndex, required_size).clone()
            return ans
        }

        private fun getLast(regex: String, l: Int, r: Int): Row {
            if (last[r] != null) return last[r]!!
            last[r] = when {
                regex[l] == '[' -> Row(parseFromBrackets(regex, l, r))
                regex[l] == '(' -> parseFromParentheses(regex, l, r)
                else -> Row(Word(regex, l, r, 0))
            }
            return last[r]!!
        }
    }

    fun clone(): Row {
        val row = Row()
        for (word in words) row.words.add(word.clone())
        return row
    }
}
