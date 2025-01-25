package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 21.09.16.
 */

data class QmsThemes(
    val userId: Int,
    val nick: String?,
    val themes: List<QmsTheme>
)
