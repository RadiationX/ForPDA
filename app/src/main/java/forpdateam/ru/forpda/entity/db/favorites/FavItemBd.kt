package forpdateam.ru.forpda.entity.db.favorites

import forpdateam.ru.forpda.entity.remote.favorites.IFavItem
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class FavItemBd : RealmObject, IFavItem {
    @PrimaryKey
    override var favId: Int = 0
    override var topicId: Int = 0
    override var forumId: Int = 0
    override var authorId: Int = 0
    override var lastUserId: Int = 0
    override var stParam: Int = 0
    override var pages: Int = 0
    override var curatorId: Int = 0
    override var trackType: String? = null
    override var infoColor: String? = null
    override var topicTitle: String? = null
    override var forumTitle: String? = null
    override var authorUserNick: String? = null
    override var lastUserNick: String? = null
    override var date: String? = null
    override var desc: String? = null
    override var curatorNick: String? = null
    override var subType: String? = null
    override var isPin: Boolean = false
    override var isForum: Boolean = false
    override var isNew: Boolean = false
    override var isPoll: Boolean = false
    override var isClosed: Boolean = false

    constructor()

    constructor(item: IFavItem) {
        favId = item.favId
        topicId = item.topicId
        forumId = item.forumId
        authorId = item.authorId
        lastUserId = item.lastUserId
        stParam = item.stParam
        pages = item.pages
        curatorId = item.curatorId

        trackType = item.trackType
        infoColor = item.infoColor
        topicTitle = item.topicTitle
        forumTitle = item.forumTitle
        authorUserNick = item.authorUserNick
        lastUserNick = item.lastUserNick
        date = item.date
        desc = item.desc
        curatorNick = item.curatorNick
        subType = item.subType

        isPin = item.isPin
        isForum = item.isForum

        isNew = item.isNew
        isPoll = item.isPoll
        isClosed = item.isClosed
    }
}
