package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal object StringSetPreferenceAdapter : PreferenceAdapter<Set<String>?> {
    override fun get(preferences: SharedPreferences, key: String, default: Set<String>?): Set<String>? {
        return preferences.getStringSet(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Set<String>?) {
        preferencesEditor.putStringSet(key, value)
    }
}