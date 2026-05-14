package forpdateam.ru.forpda.entity.db.qms

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "qms_contacts")
class QmsContactDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("nick") val nick: String,
    @ColumnInfo("avatar_url") val avatar: String,
    @ColumnInfo("count") val count: Int
)
