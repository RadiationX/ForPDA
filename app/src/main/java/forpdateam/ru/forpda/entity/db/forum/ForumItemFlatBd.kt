package forpdateam.ru.forpda.entity.db.forum

import io.github.xilinjia.krdb.types.RealmObject

/**
 * Created by radiationx on 25.03.17.
 */
class ForumItemFlatBd : RealmObject {

    var id: Int = -1
    var parentId: Int = -1
    var level: Int = -1
    var title: String? = null

    constructor()

    constructor(id: Int, parentId: Int, level: Int, title: String?) {
        this.id = id
        this.parentId = parentId
        this.level = level
        this.title = title
    }
}
