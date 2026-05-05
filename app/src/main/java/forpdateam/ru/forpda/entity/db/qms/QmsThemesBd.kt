package forpdateam.ru.forpda.entity.db.qms

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemesBd(
    @PrimaryKey
    var userId: Int = 0,
    var nick: String? = null,
    val themes: RealmList<QmsThemeBd> = realmListOf()
) : RealmObject
