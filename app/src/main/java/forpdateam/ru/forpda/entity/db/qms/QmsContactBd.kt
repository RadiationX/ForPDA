package forpdateam.ru.forpda.entity.db.qms

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsContactBd : RealmObject {

    @PrimaryKey
    var nick: String? = null
    var avatar: String? = null
    var id: Int = 0
    var count: Int = 0

    constructor()

    constructor(nick: String?, avatar: String?, id: Int, count: Int) {
        this.nick = nick
        this.avatar = avatar
        this.id = id
        this.count = count
    }
}
