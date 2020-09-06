package forpdateam.ru.forpda.entity.remote.forum

import java.util.ArrayList

/**
 * Created by radiationx on 15.02.17.
 */

class ForumItemTree {
    var id = -1
    var parentId = -1
    var level = -1
    var title: String? = null
    var forums: MutableList<ForumItemTree>? = null

    constructor() {}

    constructor(item: IForumItemFlat) {
        id = item.id
        parentId = item.parentId
        title = item.title
        level = item.level
    }

    fun addForum(item: ForumItemTree) {
        if (forums == null) {
            forums = mutableListOf()
        }
        forums?.add(item)
    }
}
