package forpdateam.ru.forpda.model.data.storage

import java.util.regex.Pattern

interface IPatternProvider {
    fun getCurrentVersion(): Int
    fun getPattern(scope: String, key: String): Pattern
    fun update(jsonString: String)
}