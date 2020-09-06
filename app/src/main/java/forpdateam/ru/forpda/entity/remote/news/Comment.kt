package forpdateam.ru.forpda.entity.remote.news

import java.util.ArrayList

/**
 * Created by radiationx on 02.09.17.
 */

class Comment {
    var id: Int = 0
    var userId: Int = 0
    var userNick: String? = null
    var date: String? = null
    var content: String? = null
    var isDeleted = false
    var isCollapsed = false
    var isCanReply = false
    val children = mutableListOf<Comment>()
    var level: Int = 0
    var karma: Karma? = null

    constructor() {}

    constructor(comment: Comment) {
        this.id = comment.id
        this.userId = comment.userId
        this.userNick = comment.userNick
        this.date = comment.date
        this.content = comment.content
        this.isDeleted = comment.isDeleted
        this.isCanReply = comment.isCanReply
        this.level = comment.level
        this.isCollapsed = comment.isCollapsed
        this.karma = comment.karma
    }

    class Karma {

        var status: Int = 0
        var count: Int = 0
        private val unknown1: Int = 0
        private val unknown2: Int = 0

        companion object {
            const val NOT_LIKED = 0
            const val LIKED = 1
            const val DISLIKED = -1
            const val FORBIDDEN = 2
        }
    }
}
