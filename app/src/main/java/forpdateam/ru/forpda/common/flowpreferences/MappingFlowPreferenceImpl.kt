package forpdateam.ru.forpda.common.flowpreferences

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map

class MappingFlowPreferenceImpl<T, R>(
    private val flowPreference: FlowPreference<T>,
    private val transformGet: (T) -> R,
    private val transformSet: (R) -> T
) : FlowPreference<R> {

    override fun get(): R {
        return transformGet.invoke(flowPreference.get())
    }

    override fun set(value: R) {
        flowPreference.set(transformSet.invoke(value))
    }

    override fun remove() {
        flowPreference.remove()
    }

    override suspend fun collect(collector: FlowCollector<R>) {
        flowPreference.map(transformGet).collect(collector)
    }
}