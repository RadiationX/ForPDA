package forpdateam.ru.forpda.entity.remote.news

import android.util.SparseArray

/**
 * Created by isanechek on 7/20/17.
 */
data class DetailsPage(
    val id: Int,
    val authorId: Int,
    val title: String,
    val author: String,
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
