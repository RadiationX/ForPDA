package forpdateam.ru.forpda.entity.db.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 06.09.17.
 */
@Entity(tableName = "history")
class HistoryItemDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Int ,
    @ColumnInfo("url") val url: String,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("timestamp") val timestamp: Long
) 
