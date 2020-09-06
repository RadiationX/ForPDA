package forpdateam.ru.forpda.entity.remote.forum

/**
 * Created by radiationx on 15.02.17.
 */

class ForumItemFlat : IForumItemFlat {
    override var id = -1
    override var parentId = -1
    override var level = -1
    override var title: String? = null

    constructor() {}

    constructor(item: ForumItemTree) {
        id = item.id
        parentId = item.parentId
        title = item.title
        level = item.level
    }

    constructor(item: IForumItemFlat) {
        id = item.id
        parentId = item.parentId
        title = item.title
        level = item.level
    }
}
