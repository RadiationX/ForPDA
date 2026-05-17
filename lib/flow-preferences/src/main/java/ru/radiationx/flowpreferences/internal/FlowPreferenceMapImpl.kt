package ru.radiationx.flowpreferences.internal

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.radiationx.flowpreferences.core.FlowPreferenceMap
import ru.radiationx.flowpreferences.core.PreferenceValue
import kotlin.time.Duration.Companion.seconds

internal class FlowPreferenceMapImpl(
    private val preferences: SharedPreferences,
    private val keysFilter: ((String) -> Boolean)?,
    private val keysFlow: Flow<String?>
) : FlowPreferenceMap<Map<String, PreferenceValue>> {

    private val updatesFlow by lazy {
        keysFlow
            .filter { key ->
                if (key != null && keysFilter != null) {
                    keysFilter.invoke(key)
                } else {
                    true
                }
            }
            .map { loadAll() }
            .stateIn(GlobalScope, SharingStarted.Companion.WhileSubscribed(1.seconds), loadAll())
    }

    override fun get(): Map<String, PreferenceValue> {
        return updatesFlow.value
    }

    override fun set(value: Map<String, PreferenceValue>) {
        preferences.edit {
            value.onEach { (key, value) ->
                if (keysFilter?.invoke(key) ?: true) {
                    putValue(key, value)
                }
            }
        }
    }

    override fun remove(keys: Iterable<String>) {
        preferences.edit {
            keys.forEach { key ->
                if (keysFilter?.invoke(key) ?: true) {
                    remove(key)
                }
            }
        }
    }

    override val value: Map<String, PreferenceValue>
        get() = updatesFlow.value

    override val replayCache: List<Map<String, PreferenceValue>>
        get() = updatesFlow.replayCache

    override suspend fun collect(collector: FlowCollector<Map<String, PreferenceValue>>): Nothing {
        updatesFlow.collect(collector)
    }

    private fun loadAll(): Map<String, PreferenceValue> {
        val result = mutableMapOf<String, PreferenceValue>()
        preferences.all.onEach { (key, value) ->
            if (key != null && keysFilter?.invoke(key) ?: true) {
                result[key] = readValue(value)
            }
        }
        return result
    }

    private fun SharedPreferences.Editor.putValue(key: String, value: PreferenceValue) {
        when (value) {
            PreferenceValue.Null -> remove(key)
            is PreferenceValue.String -> putString(key, value.value)
            is PreferenceValue.StringSet -> putStringSet(key, value.value)
            is PreferenceValue.Int -> putInt(key, value.value)
            is PreferenceValue.Long -> putLong(key, value.value)
            is PreferenceValue.Float -> putFloat(key, value.value)
            is PreferenceValue.Boolean -> putBoolean(key, value.value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readValue(value: Any?): PreferenceValue {
        val preferenceValue = when (value) {
            null -> PreferenceValue.Null
            is String -> PreferenceValue.String(value)
            is Set<*> -> PreferenceValue.StringSet(value as Set<String>)
            is Int -> PreferenceValue.Int(value)
            is Long -> PreferenceValue.Long(value)
            is Float -> PreferenceValue.Float(value)
            is Boolean -> PreferenceValue.Boolean(value)
            else -> null
        }
        return requireNotNull(preferenceValue) {
            "Unknown preference value type ${value?.let { it::class }}"
        }
    }
}