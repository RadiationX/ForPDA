package ru.radiationx.flowpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class FlowPreferenceImpl<T>(
    private val coroutineScope: CoroutineScope,
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: T,
    private val adapter: FlowPreferenceAdapter<T>,
    private val keysFlow: Flow<String?>
) : FlowPreference<T> {

    private val updatesFlow by lazy {
        keysFlow
            .filter { it == null || it == key }
            .map { loadValue() }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(1.seconds), loadValue())
    }

    override fun get(): T {
        return updatesFlow.value
    }

    override fun set(value: T) {
        preferences.edit {
            adapter.set(this, key, value)
        }
    }

    override fun remove() {
        preferences.edit {
            remove(key)
        }
    }

    override val value: T
        get() = updatesFlow.value

    override val replayCache: List<T>
        get() = updatesFlow.replayCache

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        updatesFlow.collect(collector)
    }

    private fun loadValue(): T {
        return adapter.get(preferences, key, defaultValue)
    }

}