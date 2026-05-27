package forpdateam.ru.forpda.entity.remote.devdb

import forpdateam.ru.forpda.entity.remote.others.user.User
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.DevDbCommentId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.coretypes.TopicId

/**
 * Created by radiationx on 06.08.17.
 */

data class Device(
    val id: DevDbDeviceId,
    val devicesId: DevDbDevicesId,
    val title: String,
    val brandTitle: String,
    val catTitle: String,
    val rating: Int,
    val specs: List<Specs>,
    val images: List<Image>,
    val comments: List<Comment>,
    val discussions: List<Topic>,
    val firmwares: List<Topic>,
    val news: List<Article>,
) {

    data class Image(
        val url: String,
        val fullUrl: String
    )

    data class Specs(
        val title: String,
        val specs: List<Spec>
    )

    data class Spec(
        val name: String,
        val value: String
    )

    data class Comment(
        val id: DevDbCommentId,
        val rating: Int,
        val likes: Int,
        val dislikes: Int,
        val user: User,
        val date: String,
        val text: String
    )

    data class Article(
        val id: ArticleId,
        val image: String,
        val title: String,
        val date: String,
        val desc: String?,
    )

    data class Topic(
        val id: TopicId,
        val title: String,
        val date: String,
        val desc: String?,
    )
}
