package forpdateam.ru.forpda.entity.db.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "forum_item_flat")
class ForumItemFlatDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("parent_id") val parentId: Int,
    @ColumnInfo("level") val level: Int,
    @ColumnInfo("title") val title: String,
)
