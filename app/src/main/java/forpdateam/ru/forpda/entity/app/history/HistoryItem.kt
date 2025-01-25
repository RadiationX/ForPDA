package forpdateam.ru.forpda.entity.app.history

/**
 * Created by radiationx on 01.01.18.
 */
class HistoryItem : IHistoryItem {
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
