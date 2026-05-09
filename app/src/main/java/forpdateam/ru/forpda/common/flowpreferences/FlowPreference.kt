package forpdateam.ru.forpda.common.flowpreferences

import kotlinx.coroutines.flow.Flow

interface FlowPreference<T> : Flow<T> {
    fun get(): T
    fun set(value: T)
    fun remove()
}

