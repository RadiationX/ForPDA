package forpdateam.ru.forpda.entity.db.history

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 06.09.17.
 */
@Entity(tableName = "history")
class HistoryItemBd(
    @PrimaryKey
    val id: Int = 0,
    val url: String? = null,
    val date: String? = null,
    val title: String? = null,
    val unixTime: Long = 0
) 
