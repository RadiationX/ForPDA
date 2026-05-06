package forpdateam.ru.forpda.entity.db.qms

import io.github.xilinjia.krdb.ext.realmListOf
import io.github.xilinjia.krdb.types.RealmList
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemesBd : RealmObject {

    @PrimaryKey
    var userId: Int = 0
    var nick: String? = null
    var themes: RealmList<QmsThemeBd> = realmListOf()

    constructor()

    constructor(userId: Int, nick: String?, themes: RealmList<QmsThemeBd>) {
        this.userId = userId
        this.nick = nick
        this.themes = themes
    }
}
