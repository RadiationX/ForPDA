package forpdateam.ru.forpda.entity.remote.qms

import java.util.ArrayList

/**
 * Created by radiationx on 21.09.16.
 */

class QmsThemes : IQmsThemes {
    override var userId: Int = 0
    override var nick: String? = null
    val themes = mutableListOf<QmsTheme>()

    constructor() {}

    constructor(qmsThemes: IQmsThemes) {
        userId = qmsThemes.userId
        nick = qmsThemes.nick
    }

}
