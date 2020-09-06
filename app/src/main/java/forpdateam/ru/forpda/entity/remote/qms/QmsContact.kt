package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 03.08.16.
 */
class QmsContact : IQmsContact {
    override var nick: String? = null
    override var avatar: String? = null
    override var id: Int = 0
    override var count: Int = 0

    constructor() {}

    constructor(contact: IQmsContact) {
        nick = contact.nick
        avatar = contact.avatar
        id = contact.id
        count = contact.count
    }
}
