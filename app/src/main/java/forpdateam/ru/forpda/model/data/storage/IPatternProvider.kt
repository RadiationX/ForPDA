package forpdateam.ru.forpda.model.data.storage

import ru.radiationx.regexparser.RegexParser
import java.util.regex.Pattern

interface IPatternProvider {
    fun getCurrentVersion(): Int
    fun getPattern(scope: String, key: String): Pattern
    fun getRegexParser(scope: String, key: String): RegexParser
    fun update(jsonString: String)
}