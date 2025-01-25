package forpdateam.ru.forpda.entity.db.forum

import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.IForumItemFlat
import io.realm.RealmObject

/**
 * Created by radiationx on 25.03.17.
 */
open class ForumItemFlatBd : RealmObject, IForumItemFlat {
    override var id: Int = -1
    override var parentId: Int = -1
    override var level: Int = -1
    override var title: String? = null

    constructor()

    constructor(item: ForumItemTree) {
        id = item.id
        parentId = item.parentId
        title = item.title
        level = item.level
    }
}
