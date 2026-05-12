package forpdateam.ru.forpda.entity.db.qms

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "qms_contacts")
class QmsContactBd(
    @PrimaryKey
    val id: Int,
    val nick: String?,
    val avatar: String?,
    val count: Int
)
