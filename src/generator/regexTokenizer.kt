package generator
/*
class RegexTokenizer(public val str: String) {
	enum class Type { NULL, // operator priotity order
		EMPTY,    // (...)_(...)
        ANY,      // .
        BACKREF,  // \1
        GROUP,    // ()
        MATCH,    // *
        MATCH1,   // +
        QUEST,    // ?
        SET,      // []
        UNSET,    // [^]
        SEP,      // |
	}
	public class Token(public val s: String) {
        var type = Type.NULL
        val me = s
        val children = Split(s).map { Token(it) }
	}
	val main = Token(str);
}
*/

// enum class PatternType { PROGRESS, REPEATS, PALINDROME, WORD }
class PatternTokinezer(public val str: String) { // do not consider WORD
    
}

