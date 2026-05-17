package ru.radiationx.flowpreferences

import kotlinx.coroutines.flow.StateFlow

interface FlowPreference<T> : StateFlow<T> {
    fun get(): T
    fun set(value: T)
    fun remove()
}

