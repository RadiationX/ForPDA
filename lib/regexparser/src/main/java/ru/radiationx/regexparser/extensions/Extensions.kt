package ru.radiationx.regexparser.extensions

import ru.radiationx.regexparser.RegexParser
import ru.radiationx.regexparser.adapter.JavaRegexAdapter
import ru.radiationx.regexparser.adapter.KotlinRegexAdapter
import ru.radiationx.regexparser.adapter.RegexAdapter
import ru.radiationx.regexparser.core.RegexContext
import java.util.regex.Pattern


fun Pattern.toRegexAdapter(): RegexAdapter {
    return JavaRegexAdapter(this)
}

fun Regex.toRegexAdapter(): RegexAdapter {
    return KotlinRegexAdapter(this)
}

fun Pattern.toRegexParser(context: RegexContext): RegexParser {
    return RegexParser(context, toRegexAdapter())
}

fun Regex.toRegexParser(context: RegexContext): RegexParser {
    return RegexParser(context, toRegexAdapter())
}