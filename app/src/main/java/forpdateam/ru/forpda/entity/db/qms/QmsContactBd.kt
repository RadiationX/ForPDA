package forpdateam.ru.forpda.entity.db.qms

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsContactBd(
    @PrimaryKey
    var nick: String? = null,
    var avatar: String? = null,
    var id: Int = 0,
    var count: Int = 0,
) : RealmObject
