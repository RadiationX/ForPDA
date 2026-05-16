package forpdateam.ru.forpda.entity.remote.checker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by radiationx on 28.01.18.
 */
@Serializable
data class UpdateDataJson(
    @SerialName("version_code") val code: Int,
    @SerialName("version_build") val build: Int,
    @SerialName("version_name") val name: String,
    @SerialName("build_date") val date: String,
    @SerialName("links") val links: List<UpdateLink>,
    @SerialName("important") val important: List<String>,
    @SerialName("added") val added: List<String>,
    @SerialName("fixed") val fixed: List<String>,
    @SerialName("changed") val changed: List<String>,
    @SerialName("patternsVersion") val patternsVersion: Int,
) {

    @Serializable
    data class UpdateLink(
        @SerialName("name") val name: String,
        @SerialName("url") val url: String,
        @SerialName("type") val type: String
    )
}