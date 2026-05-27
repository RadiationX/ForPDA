package forpdateam.ru.forpda.entity.remote.news

import ru.radiationx.coretypes.ArticleId

/**
 * Created by radiationx on 30.08.17.
 */

data class Material(
    val id: ArticleId,
    val title: String,
    val imageUrl: String,
)
