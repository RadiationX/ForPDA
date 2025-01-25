package forpdateam.ru.forpda.entity.db.qms

import forpdateam.ru.forpda.entity.remote.qms.IQmsThemes
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
class QmsThemesBd : RealmObject, IQmsThemes {
    @PrimaryKey
    override var userId: Int = 0
    override var nick: String? = null
    val themes: RealmList<QmsThemeBd> = RealmList()

    constructor()

    constructor(qmsThemes: QmsThemes) {
        userId = qmsThemes.userId
        nick = qmsThemes.nick
        for (theme in qmsThemes.themes) {
            themes.add(QmsThemeBd(theme))
        }
    }

    fun addTheme(theme: QmsThemeBd?) {
        themes.add(theme)
    }
}
