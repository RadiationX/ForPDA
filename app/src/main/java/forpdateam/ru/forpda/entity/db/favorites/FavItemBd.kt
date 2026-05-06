package forpdateam.ru.forpda.entity.db.favorites

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PersistedName
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class FavItemBd : RealmObject {

    @PrimaryKey
    var favId: Int = 0
    var topicId: Int = 0
    var forumId: Int = 0
    var authorId: Int = 0
    var lastUserId: Int = 0
    var stParam: Int = 0
    var pages: Int = 0
    var curatorId: Int = 0
    var trackType: String? = null
    var infoColor: String? = null
    var topicTitle: String? = null
    var forumTitle: String? = null
    var authorUserNick: String? = null
    var lastUserNick: String? = null
    var date: String? = null
    var desc: String? = null
    var curatorNick: String? = null
    var subType: String? = null

    @PersistedName(name = "pin")
    var isPin: Boolean = false
    var isForum: Boolean = false
    var isNew: Boolean = false
    var isPoll: Boolean = false
    var isClosed: Boolean = false

    constructor()

    constructor(
        favId: Int,
        topicId: Int,
        forumId: Int,
        authorId: Int,
        lastUserId: Int,
        stParam: Int,
        pages: Int,
        curatorId: Int,
        trackType: String?,
        infoColor: String?,
        topicTitle: String?,
        forumTitle: String?,
        authorUserNick: String?,
        lastUserNick: String?,
        date: String?,
        desc: String?,
        curatorNick: String?,
        subType: String?,
        isPin: Boolean,
        isForum: Boolean,
        isNew: Boolean,
        isPoll: Boolean,
        isClosed: Boolean
    ) {
        this.favId = favId
        this.topicId = topicId
        this.forumId = forumId
        this.authorId = authorId
        this.lastUserId = lastUserId
        this.stParam = stParam
        this.pages = pages
        this.curatorId = curatorId
        this.trackType = trackType
        this.infoColor = infoColor
        this.topicTitle = topicTitle
        this.forumTitle = forumTitle
        this.authorUserNick = authorUserNick
        this.lastUserNick = lastUserNick
        this.date = date
        this.desc = desc
        this.curatorNick = curatorNick
        this.subType = subType
        this.isPin = isPin
        this.isForum = isForum
        this.isNew = isNew
        this.isPoll = isPoll
        this.isClosed = isClosed
    }
}
