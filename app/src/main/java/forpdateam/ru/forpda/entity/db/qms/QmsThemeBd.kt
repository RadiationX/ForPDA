package forpdateam.ru.forpda.entity.db.qms

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemeBd : RealmObject{

    @PrimaryKey
    var id: Int = 0
    var countMessages: Int = 0
    var countNew: Int = 0
    var name: String? = null
    var date: String? = null

    constructor()

    constructor(id: Int, countMessages: Int, countNew: Int, name: String?, date: String?) {
        this.id = id
        this.countMessages = countMessages
        this.countNew = countNew
        this.name = name
        this.date = date
    }
}
