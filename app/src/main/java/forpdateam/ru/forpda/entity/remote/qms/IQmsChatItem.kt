package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 03.08.16.
 */
interface IQmsChatItem {
    val isMyMessage: Boolean
    val id: Int
    val readStatus: Boolean
    val time: String?
    val avatar: String?
    val content: String?
    val isDate: Boolean
    val date: String?
}
