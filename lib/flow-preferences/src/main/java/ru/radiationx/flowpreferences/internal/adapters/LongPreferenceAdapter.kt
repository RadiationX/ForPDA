package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal object LongPreferenceAdapter : PreferenceAdapter<Long> {
    override fun get(preferences: SharedPreferences, key: String, default: Long): Long {
        return preferences.getLong(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Long) {
        preferencesEditor.putLong(key, value)
    }
}