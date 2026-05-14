package forpdateam.ru.forpda.entity.db.qms

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "qms_threads")
data class QmsThemeBd(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val countMessages: Int,
    val countNew: Int,
    val name: String,
    val date: String,
)