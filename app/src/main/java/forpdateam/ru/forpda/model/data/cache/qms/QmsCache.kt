package forpdateam.ru.forpda.model.data.cache.qms

import forpdateam.ru.forpda.common.realm.wrapper.RealmWrapper
import forpdateam.ru.forpda.common.realm.wrapper.query
import forpdateam.ru.forpda.common.realm.wrapper.queryEquals
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class QmsCache(
    private val realm: RealmWrapper
) {

    fun observeContacts(): Flow<List<QmsContact>> = realm
        .query<QmsContactBd>()
        .flowMapAll { it.toDomain() }

    fun observeThemes(userId: Int): Flow<QmsThemes?> = realm
        .queryEquals<QmsThemesBd>("userId", userId)
        .flowMapFirst { it.toDomain() }

    suspend fun getContacts(): List<QmsContact> {
        return observeContacts().first()
    }

    suspend fun saveContacts(items: List<QmsContact>) {
        realm.write {
            delete(QmsContactBd::class)
            upsertAll(items.map { it.toDb() })
        }
    }

    suspend fun updateContact(item: QmsContact) {
        realm.write {
            upsert(item.toDb())
        }
    }

    suspend fun getThemes(userId: Int): QmsThemes {
        return observeThemes(userId).first() ?: throw Exception("Not found by userId=$userId")
    }

    suspend fun getAllThemes(): List<QmsThemes> {
        return realm
            .query<QmsThemesBd>()
            .mapAll { it.toDomain() }
    }

    suspend fun saveThemes(data: QmsThemes) = realm.write {
        val toDelete = queryEquals<QmsThemesBd>("userId", data.user.id).all()
        delete(toDelete)
        upsert(data.toDb())
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

fun QmsThemesBd.toDomain(): QmsThemes {
    return QmsThemes(
        user = User.required(userId, nick),
        themes = themes.map { it.toDomain(userId, nick) }
    )
}

fun QmsThemeBd.toDomain(userId: Int, nick: String?): QmsTheme {
    return QmsTheme(
        id = id,
        countMessages = countMessages,
        countNew = countNew,
        name = name,
        date = date,
        user = User.required(userId, nick)
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

fun QmsThemes.toDb(): QmsThemesBd {
    return QmsThemesBd(
        userId = user.id,
        nick = user.nick,
        themes = themes.map { it.toDb() }.toRealmList()
    )
}

fun QmsTheme.toDb(): QmsThemeBd {
    return QmsThemeBd(
        id = id,
        countMessages = countMessages,
        countNew = countNew,
        name = name,
        date = date
    )
}