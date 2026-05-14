package forpdateam.ru.forpda.model.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import forpdateam.ru.forpda.entity.db.ForumUserDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteForumDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteIdDb
import forpdateam.ru.forpda.entity.db.favorites.FavoriteTopicDb
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatDb
import forpdateam.ru.forpda.entity.db.history.HistoryItemDb
import forpdateam.ru.forpda.entity.db.notes.NoteItemDb
import forpdateam.ru.forpda.entity.db.qms.QmsContactDb
import forpdateam.ru.forpda.entity.db.qms.QmsThemeDb

@Database(
    entities = [
        FavoriteIdDb::class,
        FavoriteTopicDb::class,
        FavoriteForumDb::class,
        ForumItemFlatDb::class,
        ForumUserDb::class,
        HistoryItemDb::class,
        NoteItemDb::class,
        QmsContactDb::class,
        QmsThemeDb::class
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun favoritesIdsDao(): FavoriteIdsDao
    abstract fun favoriteTopicsDao(): FavoriteTopicsDao
    abstract fun favoriteForumsDao(): FavoriteForumsDao
    abstract fun forumsDao(): ForumsDao
    abstract fun forumUsersDao(): ForumUsersDao
    abstract fun historyDao(): HistoryDao
    abstract fun notesDao(): NotesDao
    abstract fun qmsContactsDao(): QmsContactsDao
    abstract fun qmsThemesDao(): QmsThemesDao
}