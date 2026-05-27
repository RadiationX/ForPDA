package forpdateam.ru.forpda.entity.remote.news

import android.util.SparseArray
import forpdateam.ru.forpda.entity.remote.others.user.User
import ru.radiationx.coretypes.ArticleId

/**
 * Created by isanechek on 7/20/17.
 */
data class DetailsPage(
    val id: ArticleId,
    val author: User,
    val title: String,
    val date: String,
    val imgUrl: String,
    val commentsCount: Int,
    val tags: List<Tag>,
    val karmaMap: SparseArray<Comment.Karma>,

    // for details
    val html: String,
    val materials: List<Material>,
    val commentsSource: String?,
) 
