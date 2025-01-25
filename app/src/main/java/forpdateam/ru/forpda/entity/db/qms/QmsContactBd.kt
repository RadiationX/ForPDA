package forpdateam.ru.forpda.entity.db.qms

import forpdateam.ru.forpda.entity.remote.qms.IQmsContact
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsContactBd : RealmObject, IQmsContact {
    @PrimaryKey
    override var nick: String? = null
    override var avatar: String? = null
    override var id: Int = 0
    override var count: Int = 0

    constructor()

    constructor(contact: IQmsContact) {
        nick = contact.nick
        avatar = contact.avatar
        id = contact.id
        count = contact.count
    }
}
