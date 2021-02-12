package solver.row

fun main() {}
private fun getRepChar(regex: String, i: Int): Char {
    if (isRepChar(regex[i])) return regex[i]
    return '1'
}

private fun isRepChar(c: Char) = c == '?' || c == '+' || c == '*'
private fun Boolean.toInt() = if (this) 1 else 0
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

class Row() {
    class Word(val size: Int = 0) {

        constructor(regex: String, l: Int, r: Int, q_mask: UInt) : this(0) {
            // constructor supposes there are only ?[^].ABC in regex
            var i = l - 1
            while (i++ < r) {
                if (i + 1 < r && regex[i + 1] == '?' && (q_mask and 1u shl (i - l)) != 0u) {
                    continue
                }
                if (regex[i] == '?') continue
                if (regex[i] == '[') {
                    val cur_r = findClose(regex, i)
                    letters.add(getFromBrackets(regex, i, cur_r).letters[0])
                    i = cur_r - 1
                    continue
                }
                letters.add(letter())
                if (regex[i] == '.') {
                    letters.last().set()
                } else {
                    letters.last().set(regex[i] - 'A')
                }
            }
        }

        constructor(word: Word) : this(word.size) {
            for (expr in word.sub_exprs) sub_exprs.add(expr)
            var cnt_distinct = 0
            for (letter in word.letters) if (letter.number == 0) letter.number = ++cnt_distinct
            val memory = MutableList<letter?>(cnt_distinct + 1) { null }
            for (i in 0 until size) {
                val letter = memory[word.letters[i].number]
                if (letter == null) {
                    letters[i] = word.letters[i]
                    memory[word.letters[i].number] = letters[i]
                } else {
                    letters[i] = letter
                }
            }
            for (i in word.letters.indices) word.letters[i].number = 0
        }

        class sub_expr(val l: Int = 0, val r: Int = 0) {
            val size: Int get() = r - l
        }

        class letter {
            var chars: UInt = 0u
            var number: Int = 0
            fun set() {
                chars = (1u shl 26) - 1u
            }

            fun set(p: Int) {
                chars = chars or (1u shl p)
            }

            fun reset(p: Int) {
                chars = chars and (1u shl p).inv()
            }
        }

        val sub_exprs = mutableListOf<sub_expr>()
        val letters = MutableList(size) { letter() }

        //supposes there are only ^ABC in regex and (regex[l] == '[' && regex[r - 1] == ']')
    }


    val words = mutableListOf<Word>()

    fun set_chars(allowed: UInt, idx: Int): Boolean {
        var changed = false
        for (word in words) changed = changed || (word.letters[idx].chars != allowed)
        for (word in words) word.letters[idx].chars = word.letters[idx].chars and allowed
        clean()
        return changed
    }

    constructor(word: Word) : this() {
        words.add(word)
    }


    //creates row from std::string regex regex should be a correct regex function
    //-supposes there are no +*, back references and nested parentheses in parentheses
    //-supposes there are no - in []
    //-supposes that AB..YZ and . are the only letter characters


    fun append(row: Row): Row {
        for (word in row.words) words.add(word)
        return this
    }

    fun multiply_concatenated_with(row: Row, size: Int): Row {
        val result = Row()
        val left_cnt = words.size
        val right_cnt = row.words.size
        for (i in 0 until left_cnt) {
            for (j in 0 until right_cnt) {
                val left_size = words[i].size
                val right_size = row.words[j].size
                if (left_size + right_size != size) continue
                result.words.add(words[i])
                for (letter in row.words[j].letters) {
                    result.words.last().letters.add(letter)
                }
                for (sub_expr in row.words[j].sub_exprs) {
                    result.words.last().sub_exprs.add(Word.sub_expr(sub_expr.l + left_size, sub_expr.r + left_size))
                }
            }
        }
        return result
    }

    fun clean() = words.removeAll { word -> word.letters.any { letter -> letter.chars == 0u } }

    fun char_or(idx: Int) = words.fold(0u) { acc, word -> acc or word.letters[idx].chars }

    fun distinct_lengths() = List(words.size) { words[it].size }.sorted().distinct()
}

private fun getFromBrackets(regex: String, l: Int, r: Int): Row.Word {
    assert(regex[l] == '[')
    val word = Row.Word(1)
    val invert = regex[l + 1] == '^'
    if (invert) word.letters[0].set()
    for (i in (l + invert.toInt() + 1) until r) {
        if (regex[i] == ']') return word
        if (!invert) {
            word.letters[0].set(regex[i] - 'A')
        } else {
            word.letters[0].reset(regex[i] - 'A')
        }
    }
    throw IllegalStateException("Regex $regex doesn't contain pair for opening bracket at $l")
}

var dp = mutableListOf<MutableList<Row?>>()
var last = mutableListOf<Row?>()
fun getConcatenated(regex: String, r: Int, row: Row, required_size: Int): Row {
    val lengths = row.distinct_lengths()
    val result = Row()
    for (len in lengths) {
        result.words.addAll(
            get_from_regex(regex, r, required_size - len).multiply_concatenated_with(
                row,
                required_size
            ).words
        )
    }
    return result
}

fun get_from_regex(regex: String, r: Int, required_size: Int): Row {
    if (r == 0 && required_size == 0) return Row(Row.Word())
    if (r <= 0 || required_size < 0) return Row()
    val memo = dp[required_size][r]
    if (memo != null) return memo
    val rep = getRepChar(regex, r - 1)
    val actual_r = r - isRepChar(regex[r - 1]).toInt()
    val mid = findOpen(regex, actual_r - 1)
    val result = Row()
    if (isDigit(regex[actual_r - 1])) { //back reference
        if (rep == '+' || rep == '*') {
            result.append(getConcatenated(regex, actual_r + 1, regex[actual_r - 1] - '0', required_size))
        }
        if (rep == '1') {
            result.append(getConcatenated(regex, mid, regex[actual_r - 1] - '0', required_size))
        }
    } else {
        val lastExpr = get_last(regex, mid, actual_r)
        if (rep == '+' || rep == '*') {
            result.append(getConcatenated(regex, actual_r + 1, lastExpr, required_size))
        }
        if (rep == '1') {
            result.append(getConcatenated(regex, mid, lastExpr, required_size))
        }
    }
    if (rep == '+' || rep == '?') {
        result.append(get_from_regex(regex, actual_r, required_size))
    }
    if (rep == '*' || rep == '?') {
        result.append(get_from_regex(regex, mid, required_size))
    }
    dp[required_size][actual_r + isRepChar(rep).toInt()] = result
    return result
}

private fun getConcatenated(regex: String, r: Int, group: Int, required_size: Int): Row {
    val groupL = regex.indices.filter { regex[it] == '(' }.getOrNull(group + 1)
        ?: throw IllegalStateException("Regex $regex doesn't contain ${group}th group in parentheses")
    val groupR = findClose(regex, groupL)
    val temp = getFromParentheses(regex, groupL, groupR)
    val lengths = temp.distinct_lengths()
    val result = Row()
    for (len in lengths) {
        val left = get_from_regex(regex, r, required_size - len)
        for (word in left.words) {
            val expr = word.sub_exprs[group - 1]
            if (expr.size != len) continue
            result.words.add(word)
            for (i in expr.l until expr.r) {
                result.words.last().letters.add(result.words.last().letters[i])
            }
        }
    }
    return result
}

//supposes there are no nested parentheses
//supposes that there are only (|) and word(regex, l, r, q_mask) allowed characters in regex
fun getFromParentheses(regex: String, l: Int, r: Int): Row {
    assert(regex[l] == '(')
    val row = Row()
    var cur_l = l + 1
    for (i in l until r) {
        if (regex[i] == '|' || regex[i] == ')') {
            for (mask in 0u until (1u shl regex.slice(cur_l until i).count { it == '?' })) {
                row.words.add(Row.Word(regex, cur_l, i, mask))
                row.words.last().sub_exprs.add(Row.Word.sub_expr(0, row.words.last().size))
            }
            cur_l = i + 1
        }
        if (regex[i] == ')') return row
    }
    throw IllegalStateException("Regex $regex doesn't contain pair for opening parenthesis at $l")
}

fun from_regex(regex: String, required_size: Int): Row {
    dp = MutableList(required_size + 1) { MutableList(regex.length + 1) { null } }
    last = MutableList(regex.length + 1) { null }
    return get_from_regex(regex, regex.length, required_size)
}

fun get_last(regex: String, l: Int, r: Int): Row {
    if (last[r] != null) return last[r]!!
    last[r] = when {
        regex[l] == '[' -> Row(getFromBrackets(regex, l, r))
        regex[l] == '(' -> getFromParentheses(regex, l, r)
        else -> Row(Row.Word(regex, l, r, 0u))
    }
    return last[r]!!
}
