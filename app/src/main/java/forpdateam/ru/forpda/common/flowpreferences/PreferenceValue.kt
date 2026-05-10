package forpdateam.ru.forpda.common.flowpreferences

sealed interface PreferenceValue {
    data object Null : PreferenceValue
    data class String(val value: kotlin.String) : PreferenceValue
    data class StringSet(val value: Set<kotlin.String>) : PreferenceValue
    data class Int(val value: kotlin.Int) : PreferenceValue
    data class Long(val value: kotlin.Long) : PreferenceValue
    data class Float(val value: kotlin.Float) : PreferenceValue
    data class Boolean(val value: kotlin.Boolean) : PreferenceValue
}