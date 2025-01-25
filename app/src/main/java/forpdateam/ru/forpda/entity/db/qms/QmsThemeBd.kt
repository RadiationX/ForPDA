package forpdateam.ru.forpda.entity.db.qms

import forpdateam.ru.forpda.entity.remote.qms.IQmsTheme
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemeBd : RealmObject, IQmsTheme {
    @PrimaryKey
    override var id: Int = 0
    override var countMessages: Int = 0
    override var countNew: Int = 0
    override var name: String? = null
    override var date: String? = null

    constructor()

    constructor(qmsTheme: IQmsTheme) {
        id = qmsTheme.id
        countMessages = qmsTheme.countMessages
        countNew = qmsTheme.countNew
        name = qmsTheme.name
        date = qmsTheme.date
    }
}
