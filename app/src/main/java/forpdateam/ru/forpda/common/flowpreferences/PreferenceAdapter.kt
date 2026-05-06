package forpdateam.ru.forpda.common.flowpreferences

import android.content.SharedPreferences

interface FlowPreferenceAdapter<T> {
    fun get(preferences: SharedPreferences, key: String, default: T): T
    fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: T)
}

object StringPreferenceAdapter : FlowPreferenceAdapter<String?> {
    override fun get(preferences: SharedPreferences, key: String, default: String?): String? {
        return preferences.getString(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: String?) {
        preferencesEditor.putString(key, value)
    }
}

object StringSetPreferenceAdapter : FlowPreferenceAdapter<Set<String>?> {
    override fun get(preferences: SharedPreferences, key: String, default: Set<String>?): Set<String>? {
        return preferences.getStringSet(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Set<String>?) {
        preferencesEditor.putStringSet(key, value)
    }
}

object IntPreferenceAdapter : FlowPreferenceAdapter<Int> {
    override fun get(preferences: SharedPreferences, key: String, default: Int): Int {
        return preferences.getInt(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Int) {
        preferencesEditor.putInt(key, value)
    }
}

object LongPreferenceAdapter : FlowPreferenceAdapter<Long> {
    override fun get(preferences: SharedPreferences, key: String, default: Long): Long {
        return preferences.getLong(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Long) {
        preferencesEditor.putLong(key, value)
    }
}

object FloatPreferenceAdapter : FlowPreferenceAdapter<Float> {
    override fun get(preferences: SharedPreferences, key: String, default: Float): Float {
        return preferences.getFloat(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Float) {
        preferencesEditor.putFloat(key, value)
    }
}

object BooleanPreferenceAdapter : FlowPreferenceAdapter<Boolean> {
    override fun get(preferences: SharedPreferences, key: String, default: Boolean): Boolean {
        return preferences.getBoolean(key, default)
    }

    override fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: Boolean) {
        preferencesEditor.putBoolean(key, value)
    }
}