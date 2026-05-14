package forpdateam.ru.forpda.entity.db.notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by radiationx on 06.09.17.
 */
@Entity(tableName = "notes")
class NoteItemDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Long,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("link") val link: String,
    @ColumnInfo("content") val content: String,
)
