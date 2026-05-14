package forpdateam.ru.forpda.entity.remote.forum

/**
 * Created by radiationx on 15.02.17.
 */

data class ForumItemFlat(
    val id: Int,
    val parentId: Int,
    val level: Int,
    val title: String
)
