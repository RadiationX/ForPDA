package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal class EnumPreferenceAdapter<T : Enum<T>>(private val clazz: Class<T>) : PreferenceAdapter<T> {
    override fun get(preferences: SharedPreferences, key: String, default: T): T {
        val value = preferences.getString(key, null) ?: return default
        return java.lang.Enum.valueOf(clazz, value)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: T) {
        preferencesEditor.putString(key, value.name)
    }
}