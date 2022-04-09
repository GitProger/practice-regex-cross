package generator

fun estimateCostRegex(regex: String): Int {
    // todo: find some less clown-ish estimation
    return regex.count { it !in 'A'..'Z' }
}
