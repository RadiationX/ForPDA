package forpdateam.ru.forpda.common.flowpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class FlowPreferences(
    private val coroutineScope: CoroutineScope,
    private val preferences: SharedPreferences
) {

    private val keysFlow = callbackFlow<String?> {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            trySend(key)
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.shareIn(coroutineScope, SharingStarted.Eagerly)

    fun <T> get(key: String, default: T, adapter: FlowPreferenceAdapter<T>): FlowPreference<T> {
        return FlowPreferenceImpl(preferences, key, default, adapter, keysFlow)
    }

    fun getString(key: String, default: String? = null): FlowPreference<String?> {
        return get(key, default, StringPreferenceAdapter)
    }

    fun getStringSet(key: String, default: Set<String>? = null): FlowPreference<Set<String>?> {
        return get(key, default, StringSetPreferenceAdapter)
    }

    fun getInt(key: String, default: Int = 0): FlowPreference<Int> {
        return get(key, default, IntPreferenceAdapter)
    }

    fun getLong(key: String, default: Long = 0): FlowPreference<Long> {
        return get(key, default, LongPreferenceAdapter)
    }

    fun getFloat(key: String, default: Float = 0f): FlowPreference<Float> {
        return get(key, default, FloatPreferenceAdapter)
    }

    fun getBoolean(key: String, default: Boolean = false): FlowPreference<Boolean> {
        return get(key, default, BooleanPreferenceAdapter)
    }

    fun <T : Enum<T>> getEnum(key: String, default: T, clazz: Class<T>): FlowPreference<T> {
        return get(key, default, EnumPreferenceAdapter(clazz))
    }

    fun clear() {
        preferences.edit {
            clear()
        }
    }
}


