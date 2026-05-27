package forpdateam.ru.forpda.entity.remote.theme

import forpdateam.ru.forpda.entity.remote.ForumPost
import ru.radiationx.coretypes.ForumId

/**
 * Created by radiationx on 04.08.16.
 */
data class ThemePost(
    val forumId: ForumId,
    val number: Int,
    val attachImages: List<Pair<String, String>>,
    val post: ForumPost
)
