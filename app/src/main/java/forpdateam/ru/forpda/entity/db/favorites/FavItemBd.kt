package forpdateam.ru.forpda.entity.db.favorites

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmField

/**
 * Created by radiationx on 25.03.17.
 */
open class FavItemBd(
    @PrimaryKey
    var favId: Int = 0,
    var topicId: Int = 0,
    var forumId: Int = 0,
    var authorId: Int = 0,
    var lastUserId: Int = 0,
    var stParam: Int = 0,
    var pages: Int = 0,
    var curatorId: Int = 0,
    var trackType: String? = null,
    var infoColor: String? = null,
    var topicTitle: String? = null,
    var forumTitle: String? = null,
    var authorUserNick: String? = null,
    var lastUserNick: String? = null,
    var date: String? = null,
    var desc: String? = null,
    var curatorNick: String? = null,
    var subType: String? = null,
    @RealmField(name = "pin")
    var isPin: Boolean = false,
    var isForum: Boolean = false,
    var isNew: Boolean = false,
    var isPoll: Boolean = false,
    var isClosed: Boolean = false,
) : RealmObject()
