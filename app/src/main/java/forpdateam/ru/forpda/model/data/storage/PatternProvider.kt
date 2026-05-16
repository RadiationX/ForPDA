package forpdateam.ru.forpda.model.data.storage

import ru.radiationx.regexparser.RegexParser
import java.util.regex.Pattern

interface PatternProvider {
    fun getVersion(): Int
    fun setNeedsUpdate()
    fun getPattern(scope: String, key: String): Pattern
    fun getRegexParser(scope: String, key: String): RegexParser
}