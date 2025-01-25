package forpdateam.ru.forpda.entity.db.history

import forpdateam.ru.forpda.entity.app.history.IHistoryItem
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
open class HistoryItemBd : RealmObject, IHistoryItem {
    @PrimaryKey
    override var id: Int = 0
    override var url: String? = null
    override var date: String? = null
    override var title: String? = null
    override var unixTime: Long = 0

    constructor()

    constructor(item: IHistoryItem) {
        this.id = item.id
        this.url = item.url
        this.date = item.date
        this.title = item.title
        this.unixTime = item.unixTime
    }
}
