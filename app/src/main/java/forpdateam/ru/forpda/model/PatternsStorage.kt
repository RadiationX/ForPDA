package forpdateam.ru.forpda.model

import android.content.Context
import forpdateam.ru.forpda.common.di.DataPreferences
import ru.radiationx.flowpreferences.FlowPreferences
import ru.radiationx.flowpreferences.mapping
import forpdateam.ru.forpda.entity.remote.checker.PatternsDataJson
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PatternsStorage @Inject constructor(
    private val context: Context,
    @param:DataPreferences private val preferences: FlowPreferences,
    private val json: Json,
) {
    companion object {
        private const val KEY_PATTERNS = "regex_patterns_v2"
    }

    private val patterns by lazy {
        preferences.getString(KEY_PATTERNS).mapping(
            transformGet = {
                if (it == null) {
                    return@mapping null
                }
                json.decodeFromString<PatternsDataJson>(it)
            },
            transformSet = {
                if (it == null) {
                    return@mapping null
                }
                json.encodeToString(it)
            }
        )
    }

    fun get(): PatternsDataJson? {
        return patterns.get()
    }

    fun set(value: PatternsDataJson) {
        patterns.set(value)
    }

    fun getFromAssets(): PatternsDataJson {
        return context
            .assets
            .open("patterns.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString(it) }
    }

}