package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal object StringPreferenceAdapter : PreferenceAdapter<String?> {
    override fun get(preferences: SharedPreferences, key: String, default: String?): String? {
        return preferences.getString(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: String?) {
        preferencesEditor.putString(key, value)
    }
}