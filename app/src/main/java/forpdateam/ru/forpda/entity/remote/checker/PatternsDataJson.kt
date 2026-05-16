package forpdateam.ru.forpda.entity.remote.checker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatternsDataJson(
    @SerialName("version") val version: Int,
    @SerialName("scopes") val scopes: List<Scope>
) {

    @Serializable
    data class Scope(
        @SerialName("scope") val name: String,
        @SerialName("patterns") val patterns: List<Pattern>
    ) {

    }

    @Serializable
    data class Pattern(
        @SerialName("key") val key: String,
        @SerialName("value") val value: String
    )
}