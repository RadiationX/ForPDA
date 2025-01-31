package forpdateam.ru.forpda.entity.remote.reputation

/**
 * Created by radiationx on 20.03.17.
 */

data class RepItem(
    val userId: Int,
    val title: String,
    val userNick: String,
    val sourceUrl: String?,
    val sourceTitle: String?,
    val image: String,
    val date: String,
)
