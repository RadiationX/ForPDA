package forpdateam.ru.forpda.model.data.storage

import forpdateam.ru.forpda.model.data.storage.parser.ParserPattern
import java.util.regex.Pattern

interface IPatternProvider {
    fun getCurrentVersion(): Int
    fun getPattern(scope: String, key: String): Pattern
    fun getParserPattern(scope: String, key: String): ParserPattern
    fun update(jsonString: String)
}