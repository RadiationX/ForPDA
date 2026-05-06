package forpdateam.ru.forpda.entity.db.history

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
class HistoryItemBd : RealmObject {

    @PrimaryKey
    var id: Int = 0
    var url: String? = null
    var date: String? = null
    var title: String? = null
    var unixTime: Long = 0

    constructor()

    constructor(id: Int, url: String?, date: String?, title: String?, unixTime: Long) {
        this.id = id
        this.url = url
        this.date = date
        this.title = title
        this.unixTime = unixTime
    }
}
