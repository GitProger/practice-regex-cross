package solver.row

private fun Boolean.toInt() = if (this) 1 else 0

class Row() {
    override fun toString() = "${words.size}\n" + words.joinToString("\n") { it.toString() }
    class Word() {
        val size: Int get() = letters.size
        override fun toString(): String {
            return letters.joinToString(" ") { it.toString() }
        }

        /**
         * The constructor supposes there are only ?[^].ABC in regex
         */
        constructor(regex: String, l: Int, r: Int, qMask: UInt) : this() {
            val skip = regex.indices.filter { regex[it] == '?' }.filterIndexed { q, _ -> qMask and (1u shl q) != 0u }
            var i = l - 1
            while (++i <= r) {
                if (i + 1 <= r && regex[i + 1] == '?' && (i + 1) in skip) continue

                if (regex[i] == '?') continue
                if (regex[i] == '[') {
                    val curR = findClose(regex, i)
                    letters.add(getFromBrackets(regex, i, curR).letters[0])
                    i = curR - 1
                    continue
                }
                letters.add(Letter())
                if (regex[i] == '.') {
                    letters.last().setAll()
                } else {
                    letters.last()[regex[i] - 'A'] = true
                }
            }
        }

        fun clone(): Word {
            val clone = Word()
            for (expr in subExprs) clone.subExprs.add(expr)
            for (i in letters.indices) letters[i].number = -1
            var cntDistinct = 0
            for (letter in letters) if (letter.number == -1) letter.number = cntDistinct++
            val memory = MutableList<Letter?>(cntDistinct) { null }
            for (i in 0 until size) {
                val letter = memory[letters[i].number]
                if (letter != null) {
                    clone.letters.add(letter)
                } else {
                    clone.letters.add(letters[i].clone())
                    memory[letters[i].number] = clone.letters[i]
                }
            }
            return clone
        }

        class SubExpr(val l: Int, val r: Int) {
            val size: Int get() = r - l + 1
        }

        class Letter(var chars: UInt = 0u) {
            override fun toString() =
                (0 until 26).filter { (1u shl it) and chars != 0u }.map { 'A' + it }.joinToString("")

            var number: Int = 0
            fun setAll() {
                chars = (1u shl 26) - 1u
            }

            operator fun set(p: Int, b: Boolean) {
                chars = if (b) chars or (1u shl p) else chars and (1u shl p).inv()
            }

            fun clone() = Letter(chars)
        }

        val subExprs = mutableListOf<SubExpr>()
        val letters = mutableListOf<Letter>()
    }

    val words = mutableListOf<Word>()

    fun setChars(allowed: UInt, idx: Int): Boolean {
        var changed = false
        for (word in words) changed = changed || (word.letters[idx].chars != allowed)
        for (word in words) word.letters[idx].chars = word.letters[idx].chars and allowed
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

    fun multiplyConcatenatedWith(row: Row, size: Int): Row {
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
                for (sub_expr in row.words[j].subExprs) {
                    result.words.last().subExprs.add(Word.SubExpr(sub_expr.l + leftSize, sub_expr.r + leftSize))
                }
            }
        }
        return result
    }

    private fun clean() = words.removeAll { word -> word.letters.any { letter -> letter.chars == 0u } }

    fun charOr(idx: Int) = words.fold(0u) { acc, word -> acc or word.letters[idx].chars }

    fun distinctLengths() = List(words.size) { words[it].size }.sorted().distinct()

    companion object Getter{
        private var dp = mutableListOf<MutableList<Row?>>()
        private var last = mutableListOf<Row?>()

        private fun getRepChar(regex: String, i: Int) = if (isRepChar(regex[i])) regex[i] else '1'
        private fun isRepChar(c: Char) = c == '?' || c == '+' || c == '*'
        private fun isDigit(c: Char) = c in '0'..'9'

        private fun findOpen(s: String, closing: Int): Int {
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
        private fun getFromBrackets(regex: String, l: Int, r: Int): Word {
            assert(regex[l] == '[')
            val word = Word()
            word.letters.add(Word.Letter())
            val invert = regex[l + 1] == '^'
            if (invert) word.letters[0].setAll()
            for (i in (l + invert.toInt() + 1)..r) {
                if (regex[i] == ']') return word.also { assert(i == r) }
                word.letters[0][regex[i] - 'A'] = !invert
            }
            throw IllegalStateException("Regex $regex doesn't contain pair for opening bracket at $l")
        }

        private fun getConcatenated(regex: String, r: Int, row: Row, required_size: Int): Row {
            val lengths = row.distinctLengths()
            val result = Row()
            for (len in lengths) {
                result.words.addAll(
                    getFromRegex(regex, r, required_size - len).multiplyConcatenatedWith(row, required_size).words
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
        private fun getFromRegex(regex: String, r: Int, required_size: Int): Row {
            if (r == -1 && required_size == 0) return Row(Word())
            if (r <= -1 || required_size < 0) return Row()
            val memo = dp[required_size][r]
            if (memo != null) return memo
            val rep = getRepChar(regex, r)
            return getFromRegexWithRep(regex, r - isRepChar(regex[r]).toInt(), rep, required_size)
        }

        private fun getFromRegexWithRep(regex: String, r: Int, rep: Char, required_size: Int): Row {
            val mid = findOpen(regex, r)
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
                result.append(getFromRegex(regex, r, required_size))
            }
            if (rep == '*' || rep == '?') {
                result.append(getFromRegex(regex, mid - 1, required_size))
            }
            dp[required_size][r + isRepChar(rep).toInt()] = result
            return result
        }

        private fun getConcatenated(regex: String, r: Int, group: Int, required_size: Int): Row {
            fun ending(ord: Int) = when(ord) {
                1 -> "st"
                2 -> "nd"
                else -> "th"
            }
            val groupL = regex.indices.filter { regex[it] == '(' }.getOrNull(group - 1)
                ?: throw IllegalStateException("Regex $regex doesn't contain ${group}${ending(group)} group in parentheses")
            val groupR = findClose(regex, groupL)
            val temp = getFromParentheses(regex, groupL, groupR)
            val lengths = temp.distinctLengths()
            val result = Row()
            for (len in lengths) {
                val left = getFromRegex(regex, r, required_size - len)
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
        private fun getFromParentheses(regex: String, l: Int, r: Int): Row {
            assert(regex[l] == '(')
            val row = Row()
            var curL = l + 1
            for (i in l + 1..r) {
                if (regex[i] == '|' || regex[i] == ')') {
                    for (mask in 0u until (1u shl regex.slice(curL until i).count { it == '?' })) {
                        row.words.add(Word(regex, curL, i - 1, mask))
                        row.words.last().subExprs.add(Word.SubExpr(0, row.words.last().size - 1))
                    }
                    curL = i + 1
                    if (regex[i] == ')') return row
                }
            }
            throw IllegalStateException("Regex $regex doesn't contain pair for opening parenthesis at $l")
        }

        fun fromRegex(regex: String, required_size: Int): Row {
            dp = MutableList(required_size + 1) { MutableList(regex.length + 1) { null } }
            last = MutableList(regex.length + 1) { null }
            return getFromRegex(regex, regex.lastIndex, required_size)
        }

        private fun getLast(regex: String, l: Int, r: Int): Row {
            if (last[r] != null) return last[r]!!
            last[r] = when {
                regex[l] == '[' -> Row(getFromBrackets(regex, l, r))
                regex[l] == '(' -> getFromParentheses(regex, l, r)
                else -> Row(Word(regex, l, r, 0u))
            }
            return last[r]!!
        }
    }
}
