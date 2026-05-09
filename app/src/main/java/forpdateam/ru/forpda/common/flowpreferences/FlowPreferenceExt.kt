package forpdateam.ru.forpda.common.flowpreferences

fun <T, R> FlowPreference<T>.mapping(
    transformGet: (T) -> R,
    transformSet: (R) -> T
): FlowPreference<R> {
    return MappingFlowPreferenceImpl(this, transformGet, transformSet)
}