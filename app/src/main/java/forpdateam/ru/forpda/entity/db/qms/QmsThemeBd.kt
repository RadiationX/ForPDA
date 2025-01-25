package forpdateam.ru.forpda.entity.db.qms

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
open class QmsThemeBd(
    @PrimaryKey
    var id: Int = 0,
    var countMessages: Int = 0,
    var countNew: Int = 0,
    var name: String? = null,
    var date: String? = null,
) : RealmObject()
