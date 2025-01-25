package forpdateam.ru.forpda.entity.app.history

/**
 * Created by radiationx on 01.01.18.
 */
interface IHistoryItem {
    var id: Int

    var url: String?

    var date: String?

    var title: String?

    var unixTime: Long
}
