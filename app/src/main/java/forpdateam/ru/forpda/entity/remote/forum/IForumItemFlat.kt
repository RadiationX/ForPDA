package forpdateam.ru.forpda.entity.remote.forum

/**
 * Created by radiationx on 25.03.17.
 */

interface IForumItemFlat {
    var id: Int
    var parentId: Int
    var title: String?
    var level: Int
}
