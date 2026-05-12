package forpdateam.ru.forpda.entity.db.forum

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by radiationx on 25.03.17.
 */
@Entity(tableName = "forum_item_flat")
class ForumItemFlatBd(
    @PrimaryKey
    val id: Int,
    val parentId: Int,
    val level: Int,
    val title: String?,
)
