package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal object BooleanPreferenceAdapter : PreferenceAdapter<Boolean> {
    override fun get(preferences: SharedPreferences, key: String, default: Boolean): Boolean {
        return preferences.getBoolean(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Boolean) {
        preferencesEditor.putBoolean(key, value)
    }
}