package ru.radiationx.flowpreferences.internal.adapters

import android.content.SharedPreferences
import ru.radiationx.flowpreferences.core.PreferenceAdapter

internal object FloatPreferenceAdapter : PreferenceAdapter<Float> {
    override fun get(preferences: SharedPreferences, key: String, default: Float): Float {
        return preferences.getFloat(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Float) {
        preferencesEditor.putFloat(key, value)
    }
}