package forpdateam.ru.forpda.entity.remote.favorites

/**
 * Created by radiationx on 25.03.17.
 */

interface IFavItem {
    var favId: Int
    var topicId: Int
    var forumId: Int
    var authorId: Int
    var lastUserId: Int
    var stParam: Int
    var pages: Int
    var curatorId: Int
    var desc: String?
    var trackType: String?
    var infoColor: String?
    var topicTitle: String?
    var forumTitle: String?
    var authorUserNick: String?
    var lastUserNick: String?
    var date: String?
    var curatorNick: String?
    var subType: String?
    var isPin: Boolean
    var isForum: Boolean
    var isNew: Boolean
    var isPoll: Boolean
    var isClosed: Boolean
}
