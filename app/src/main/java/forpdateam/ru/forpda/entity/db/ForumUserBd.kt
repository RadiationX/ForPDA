package forpdateam.ru.forpda.entity.db

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 08.07.17.
 */
class ForumUserBd : RealmObject {

    @PrimaryKey
    var id: Int = 0
    var nick: String? = ""
    var avatar: String? = ""

    constructor()

    constructor(id: Int, nick: String?, avatar: String?) {
        this.id = id
        this.nick = nick
        this.avatar = avatar
    }
}
