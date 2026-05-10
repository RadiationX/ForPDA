package forpdateam.ru.forpda.common.flowpreferences

import kotlinx.coroutines.flow.FlowCollector

class MappingFlowPreferenceMapImpl<T, R>(
    private val flowPreference: FlowPreferenceMap<T>,
    private val transformGet: (T) -> R,
    private val transformSet: (R) -> T
) : FlowPreferenceMap<R> {

    private val transformedFlow by lazy {
        TransformStateFlow(flowPreference, transformGet)
    }

    override fun get(): R {
        return transformedFlow.value
    }

    override fun set(value: R) {
        flowPreference.set(transformSet.invoke(value))
    }

    override fun remove(keys: Iterable<String>) {
        flowPreference.remove(keys)
    }

    override val value: R
        get() = transformedFlow.value

    override val replayCache: List<R>
        get() = transformedFlow.replayCache

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        transformedFlow.collect(collector)
    }
}