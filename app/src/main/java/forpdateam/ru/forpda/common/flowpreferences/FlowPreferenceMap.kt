package forpdateam.ru.forpda.common.flowpreferences

import kotlinx.coroutines.flow.StateFlow

interface FlowPreferenceMap<T> : StateFlow<T> {
    fun get(): T
    fun set(value: T)
    fun remove(keys: Iterable<String>)
}

