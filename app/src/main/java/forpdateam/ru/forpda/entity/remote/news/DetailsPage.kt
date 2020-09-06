package forpdateam.ru.forpda.entity.remote.news

import android.util.SparseArray

import java.util.ArrayList

/**
 * Created by isanechek on 7/20/17.
 */
// На время только, ибо оригинал в котлине
class DetailsPage {
    var id: Int = 0
    var commentId: Int = 0
    var authorId: Int = 0
    var url: String? = null
    var title: String? = null
    var description: String? = null
    var author: String? = null
    var date: String? = null
    var imgUrl: String? = null
    var commentsCount: Int = 0
    val tags = mutableListOf<Tag>()
    var karmaMap = SparseArray<Comment.Karma>()

    // for details

    var html: String? = null
    val materials = mutableListOf<Material>()
    var navId: String? = null
    var commentsSource: String? = null
    var commentTree: Comment? = null
}
