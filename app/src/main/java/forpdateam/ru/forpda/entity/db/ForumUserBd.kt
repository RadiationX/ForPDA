package forpdateam.ru.forpda.entity.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 08.07.17.
 */
open class ForumUserBd(
    @PrimaryKey
    var id: Int = 0,
    var nick: String? = "",
    var avatar: String? = ""
) : RealmObject()
