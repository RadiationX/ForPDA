package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal object IntPreferenceAdapter : PreferenceAdapter<Int> {
    override fun get(preferences: SharedPreferences, key: String, default: Int): Int {
        return preferences.getInt(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Int) {
        preferencesEditor.putInt(key, value)
    }
}