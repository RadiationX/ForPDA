package forpdateam.ru.forpda.entity.db.qms

import io.github.xilinjia.krdb.ext.realmListOf
import io.github.xilinjia.krdb.types.RealmList
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemesBd(
    @PrimaryKey
    var userId: Int = 0,
    var nick: String? = null,
    val themes: RealmList<QmsThemeBd> = realmListOf()
) : RealmObject
