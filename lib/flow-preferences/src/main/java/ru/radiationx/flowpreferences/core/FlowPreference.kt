package ru.radiationx.flowpreferences.core

import kotlinx.coroutines.flow.StateFlow

interface FlowPreference<T> : StateFlow<T> {
    fun get(): T
    fun set(value: T)
    fun remove()
}

