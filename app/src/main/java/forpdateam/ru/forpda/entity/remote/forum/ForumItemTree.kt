package forpdateam.ru.forpda.entity.remote.forum

/**
 * Created by radiationx on 15.02.17.
 */

data class ForumItemTree(
    val item: ForumItemFlat,
    val forums: List<ForumItemTree>
)
