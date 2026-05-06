package forpdateam.ru.forpda.common.flowpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class FlowPreference<T>(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: T,
    private val adapter: FlowPreferenceAdapter<T>,
    private val keysFlow: Flow<String?>
) : Flow<T> {

    private val updatesFlow = keysFlow
        .onStart { emit(key) }
        .filter { it == null || it == key }
        .map { get() }

    fun get(): T {
        return adapter.get(preferences, key, defaultValue)
    }

    fun set(value: T) {
        preferences.edit {
            adapter.set(this, key, value)
        }
    }

    fun remove() {
        preferences.edit {
            remove(key)
        }
    }

    override suspend fun collect(collector: FlowCollector<T>) {
        updatesFlow.collect(collector)
    }

}