package forpdateam.ru.forpda.entity.db.notes

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
class NoteItemBd(
    @PrimaryKey
    var id: Long = 0,
    var title: String? = null,
    var link: String? = null,
    var content: String? = null,
) : RealmObject
