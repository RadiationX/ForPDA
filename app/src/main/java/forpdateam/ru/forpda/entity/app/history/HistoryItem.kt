package forpdateam.ru.forpda.entity.app.history

/**
 * Created by radiationx on 01.01.18.
 */
data class HistoryItem(
    val id: Int,
    val url: String?,
    val title: String?,
    val unixTime: Long,
    val date: String?,
)
