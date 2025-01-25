package forpdateam.ru.forpda.entity.db.qms

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
open class QmsContactBd(
    @PrimaryKey
    var nick: String? = null,
    var avatar: String? = null,
    var id: Int = 0,
    var count: Int = 0,
) : RealmObject()
