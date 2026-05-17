package ru.radiationx.flowpreferences.internal

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class TransformStateFlowImpl<T, R>(
    private val parent: StateFlow<T>,
    private val transform: (T) -> R
) : StateFlow<R> {

    private val outputStateFlow by lazy {
        MutableStateFlow(transform(parent.value))
    }

    override val value: R
        get() = outputStateFlow.value

    override val replayCache: List<R>
        get() = outputStateFlow.replayCache

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        outputStateFlow.collect(collector)
    }

}