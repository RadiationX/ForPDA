package forpdateam.ru.forpda.entity.db.history

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
class HistoryItemBd(
    @PrimaryKey
    var id: Int = 0,
    var url: String? = null,
    var date: String? = null,
    var title: String? = null,
    var unixTime: Long = 0
) : RealmObject
