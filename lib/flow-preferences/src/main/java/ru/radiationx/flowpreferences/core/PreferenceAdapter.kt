package ru.radiationx.flowpreferences.core

import android.content.SharedPreferences

interface PreferenceAdapter<T> {
    fun get(preferences: SharedPreferences, key: String, default: T): T
    fun set(preferencesEditor: SharedPreferences.Editor, key: String, value: T)
}