package ru.radiationx.flowpreferences.internal

import kotlinx.coroutines.flow.FlowCollector
import ru.radiationx.flowpreferences.core.FlowPreference

internal class MappingFlowPreferenceImpl<T, R>(
    private val flowPreference: FlowPreference<T>,
    private val transformGet: (T) -> R,
    private val transformSet: (R) -> T
) : FlowPreference<R> {

    private val transformedFlow by lazy {
        TransformStateFlowImpl(flowPreference, transformGet)
    }

    override fun get(): R {
        return transformedFlow.value
    }

    override fun set(value: R) {
        flowPreference.set(transformSet.invoke(value))
    }

    override fun remove() {
        flowPreference.remove()
    }

    override val value: R
        get() = transformedFlow.value

    override val replayCache: List<R>
        get() = transformedFlow.replayCache

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        transformedFlow.collect(collector)
    }
}