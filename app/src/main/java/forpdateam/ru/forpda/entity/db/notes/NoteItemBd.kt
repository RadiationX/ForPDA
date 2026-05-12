package forpdateam.ru.forpda.entity.db.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
@Entity(tableName = "notes")
class NoteItemBd(
    @PrimaryKey
    val id: Long,
    val title: String?,
    val link: String?,
    val content: String?,
)
