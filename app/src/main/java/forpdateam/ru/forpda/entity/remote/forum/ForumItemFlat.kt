package forpdateam.ru.forpda.entity.remote.forum

import ru.radiationx.coretypes.ForumId

/**
 * Created by radiationx on 15.02.17.
 */

data class ForumItemFlat(
    val id: ForumId,
    val parentId: ForumId,
    val level: Int,
    val title: String
)
