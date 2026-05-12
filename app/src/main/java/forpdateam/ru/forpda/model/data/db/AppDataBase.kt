package forpdateam.ru.forpda.model.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import forpdateam.ru.forpda.entity.db.ForumUserBd
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd
import forpdateam.ru.forpda.entity.db.notes.NoteItemBd
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd

@Database(
    entities = [
        FavItemBd::class,
        ForumItemFlatBd::class,
        ForumUserBd::class,
        HistoryItemBd::class,
        NoteItemBd::class,
        QmsContactBd::class,
        QmsThemeBd::class
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun forumsDao(): ForumsDao
    abstract fun forumUsersDao(): ForumUsersDao
    abstract fun historyDao(): HistoryDao
    abstract fun notesDao(): NotesDao
    abstract fun qmsContactsDao(): QmsContactsDao
    abstract fun qmsThemesDao(): QmsThemesDao
}