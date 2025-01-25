package forpdateam.ru.forpda.entity.db.forum

import io.realm.RealmObject

/**
 * Created by radiationx on 25.03.17.
 */
open class ForumItemFlatBd(
    var id: Int = -1,
    var parentId: Int = -1,
    var level: Int = -1,
    var title: String? = null,
) : RealmObject()
