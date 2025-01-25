package forpdateam.ru.forpda.entity.db.qms

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
open class QmsThemesBd(
    @PrimaryKey
    var userId: Int = 0,
    var nick: String? = null,
    val themes: RealmList<QmsThemeBd> = RealmList()
) : RealmObject()
