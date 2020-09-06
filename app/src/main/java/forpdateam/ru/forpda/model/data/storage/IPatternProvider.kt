package forpdateam.ru.forpda.model.data.storage

import java.util.regex.Pattern

interface IPatternProvider {
    fun getCurrentVersion(): Int
    fun getPattern(scope: String, key: String): Pattern
    fun update(jsonString: String)
    fun update(version: Int, newData: Map<String, MutableMap<String, String>>)
    fun parse(jsonString: String): Pair<Int, Map<String, MutableMap<String, String>>>
}