package forpdateam.ru.forpda.entity.app.notes

/**
 * Created by radiationx on 06.09.17.
 */
class NoteItem : INoteItem {
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
