package forpdateam.ru.forpda.entity.remote.news

import forpdateam.ru.forpda.entity.remote.others.user.User

/**
 * Created by radiationx on 02.09.17.
 */

data class Comment(
    val id: Int,
    val user: User,
    val date: String?,
    val content: String?,
    val isDeleted: Boolean,
    val level: Int,
    val karma: Karma?,
) {

    data class Karma(
        val status: Int,
        val count: Int,
    ) {

        companion object {
            const val NOT_LIKED = 0
            const val LIKED = 1
            const val DISLIKED = -1
            const val FORBIDDEN = 2
        }
    }
}
