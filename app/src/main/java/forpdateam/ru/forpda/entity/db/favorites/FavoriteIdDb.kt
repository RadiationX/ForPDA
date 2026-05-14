package forpdateam.ru.forpda.entity.db.favorites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_ids")
data class FavoriteIdDb(
    @PrimaryKey
    @ColumnInfo("id") val id: Int
)