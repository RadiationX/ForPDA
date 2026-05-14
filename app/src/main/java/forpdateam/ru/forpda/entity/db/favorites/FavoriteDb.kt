package forpdateam.ru.forpda.entity.db.favorites

import androidx.room.Embedded
import androidx.room.Relation

data class FavoriteDb(
    @Embedded val id: FavoriteIdDb,
    @Relation(
        parentColumn = "id",
        entityColumn = "fav_id"
    )
    val topic: FavoriteTopicDb?,
    @Relation(
        parentColumn = "id",
        entityColumn = "fav_id"
    )
    val forum: FavoriteForumDb?
)