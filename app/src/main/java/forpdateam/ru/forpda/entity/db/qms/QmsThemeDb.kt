package forpdateam.ru.forpda.entity.db.qms

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "qms_threads")
data class QmsThemeDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("user_id") val userId: Int,
    @ColumnInfo("count_all") val countMessages: Int,
    @ColumnInfo("count_new") val countNew: Int,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("date") val date: String,
)