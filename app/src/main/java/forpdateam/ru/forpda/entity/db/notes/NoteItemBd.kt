package forpdateam.ru.forpda.entity.db.notes

import forpdateam.ru.forpda.entity.app.notes.INoteItem
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
class NoteItemBd : RealmObject, INoteItem {
    @PrimaryKey
    override var id: Long = 0
    override var title: String? = null
    override var link: String? = null
    override var content: String? = null

    constructor()

    constructor(item: INoteItem) {
        id = item.id
        title = item.title
        link = item.link
        content = item.content
    }
}
