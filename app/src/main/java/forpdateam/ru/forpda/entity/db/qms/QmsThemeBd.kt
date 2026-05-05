package forpdateam.ru.forpda.entity.db.qms

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemeBd(
    @PrimaryKey
    var id: Int = 0,
    var countMessages: Int = 0,
    var countNew: Int = 0,
    var name: String? = null,
    var date: String? = null,
) : RealmObject
