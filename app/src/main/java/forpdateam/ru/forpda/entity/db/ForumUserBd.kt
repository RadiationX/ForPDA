package forpdateam.ru.forpda.entity.db

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 08.07.17.
 */
class ForumUserBd(
    @PrimaryKey
    var id: Int = 0,
    var nick: String? = "",
    var avatar: String? = ""
) : RealmObject
