package forpdateam.ru.forpda.common.flowpreferences

import kotlinx.coroutines.flow.Flow

fun <T, R> FlowPreference<T>.mapping(
    transformGet: (T) -> R,
    transformSet: (R) -> T
): FlowPreference<R> {
    return MappingFlowPreferenceImpl(this, transformGet, transformSet)
}

fun <T, R> FlowPreferenceMap<T>.mapping(
    transformGet: (T) -> R,
    transformSet: (R) -> T
): FlowPreferenceMap<R> {
    return MappingFlowPreferenceMapImpl(this, transformGet, transformSet)
}

fun <T> FlowPreference<T>.asFlow(): Flow<T> {
    return this
}

fun <T> FlowPreferenceMap<T>.asFlow(): Flow<T> {
    return this
}