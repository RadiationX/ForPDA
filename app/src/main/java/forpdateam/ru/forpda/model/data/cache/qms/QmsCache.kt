package forpdateam.ru.forpda.model.data.cache.qms

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.extensions.mapInnerList
import forpdateam.ru.forpda.model.data.db.QmsContactsDao
import forpdateam.ru.forpda.model.data.db.QmsThemesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class QmsCache(
    private val qmsContactsDao: QmsContactsDao,
    private val qmsThemesDao: QmsThemesDao,
    private val database: RoomDatabase
) {

    fun observeContacts(): Flow<List<QmsContact>> {
        return qmsContactsDao.observeAll().mapInnerList { it.toDomain() }
    }

    suspend fun getContacts(): List<QmsContact> {
        return qmsContactsDao.getAll().map { it.toDomain() }
    }

    suspend fun getContact(userId: Int): QmsContact? {
        return qmsContactsDao.getByUserId(userId)?.toDomain()
    }

    suspend fun saveContacts(items: List<QmsContact>) {
        database.withTransaction {
            qmsContactsDao.deleteAll()
            qmsContactsDao.upsertAll(items.map { it.toDb() })
        }
    }

    suspend fun updateContact(item: QmsContact) {
        qmsContactsDao.upsert(item.toDb())
    }

    fun observeThemes(userId: Int): Flow<QmsThemes?> {
        return combine(
            flow = qmsContactsDao.observeByUserId(userId),
            flow2 = qmsThemesDao.observeByUserId(userId),
            transform = { contact, themes ->
                contact?.let {
                    themes.toDomain(it)
                }
            }
        )
    }

    suspend fun getThemes(userId: Int): QmsThemes {
        val contact = qmsContactsDao.getByUserId(userId) ?: throw Exception("Not found by userId=$userId")
        val themes = qmsThemesDao.getByUserId(userId)
        return themes.toDomain(contact)
    }

    suspend fun getAllThemes(): List<QmsThemes> {
        return qmsContactsDao.getAll().map {
            val themes = qmsThemesDao.getAll()
            themes.toDomain(it)
        }
    }

    suspend fun saveThemes(data: QmsThemes) {
        database.withTransaction {
            val contact = qmsContactsDao.getByUserId(data.user.id)
            if (contact == null) {
                val newContact = QmsContactBd(
                    id = data.user.id,
                    nick = data.user.nick,
                    avatar = null,
                    count = data.themes.sumOf { it.countNew }
                )
                qmsContactsDao.upsert(newContact)
            }
            qmsThemesDao.deleteByUserId(data.user.id)
            qmsThemesDao.upsertAll(data.themes.map { it.toDb(data.user.id) })
        }
    }
}

fun QmsContactBd.toDomain(): QmsContact {
    return QmsContact(
        user = ForumUser.required(
            id = id,
            nick = nick,
            avatar = avatar
        ),
        count = count
    )
}

fun QmsThemeBd.toDomain(): QmsTheme {
    return QmsTheme(
        id = id,
        countMessages = countMessages,
        countNew = countNew,
        name = name,
        date = date,
    )
}

fun List<QmsThemeBd>.toDomain(contact: QmsContactBd): QmsThemes {
    return QmsThemes(
        user = User.required(contact.id, contact.nick),
        themes = map { it.toDomain() }
    )
}

fun QmsContact.toDb(): QmsContactBd {
    return QmsContactBd(
        nick = user.nick,
        avatar = user.avatar,
        id = user.id,
        count = count
    )
}

fun QmsTheme.toDb(userId: Int): QmsThemeBd {
    return QmsThemeBd(
        id = id,
        userId = userId,
        countMessages = countMessages,
        countNew = countNew,
        name = name,
        date = date
    )
}