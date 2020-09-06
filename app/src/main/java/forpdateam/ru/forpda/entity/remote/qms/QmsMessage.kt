package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 03.08.16.
 */
class QmsMessage : IQmsChatItem {
    override var isMyMessage = false
    override var isDate = false
    override var id: Int = 0
    override var readStatus = false
    override var time: String? = null
    override var avatar: String? = null
    override var date: String? = null
    override var content: String? = null
}
