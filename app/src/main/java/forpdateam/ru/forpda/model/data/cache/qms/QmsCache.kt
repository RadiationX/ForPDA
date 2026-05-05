package forpdateam.ru.forpda.model.data.cache.qms

import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import io.realm.Realm
import io.realm.RealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class QmsCache {

    private val contactsFlow = MutableStateFlow<List<QmsContact>?>(null)
    private val themesFlows = mutableMapOf<Int, MutableStateFlow<QmsThemes?>>()

    fun observeContacts(): Flow<List<QmsContact>> = contactsFlow.filterNotNull()
    fun observeThemes(userId: Int): Flow<QmsThemes> = getOrCreateThemesRelay(userId).filterNotNull()

    fun getContacts(): List<QmsContact> = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsContactBd::class.java).findAll().map { it.toDomain() }
    }.also {
        if (contactsFlow.value == null) {
            contactsFlow.value = it
        }
    }

    fun saveContacts(items: List<QmsContact>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(QmsContactBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { it.toDb() })
        }
        contactsFlow.value = getContacts()
    }

    fun updateContact(item: QmsContact) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.copyToRealmOrUpdate(item.toDb())
        }
        if (contactsFlow.value != null) {
            realm.where(QmsContactBd::class.java)
                .equalTo("id", item.user.id)
                .findFirst()
                ?.also { newItem ->
                    val currentItems = contactsFlow.value!!.toMutableList()
                    val index = currentItems.indexOfFirst { newItem.id == it.user.id }
                    if (index == -1) {
                        contactsFlow.value = getContacts()
                    } else {
                        currentItems[index] = newItem.toDomain()
                        contactsFlow.value = currentItems
                    }
                }
        }
    }


    fun getThemes(userId: Int): QmsThemes = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsThemesBd::class.java).equalTo("userId", userId).findAll().last()
            ?.toDomain()
            ?: throw Exception("Not found by userId=$userId")
    }.also { themes ->
        getOrCreateThemesRelay(userId).also {
            if (it.value == null) {
                it.value = themes
            }
        }
    }

    fun getAllThemes(): List<QmsThemes> = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsThemesBd::class.java).findAll().map { themesDb ->
            themesDb.toDomain()
        }
    }.onEach { themes ->
        getOrCreateThemesRelay(themes.user.id).also {
            if (it.value == null) {
                it.value = themes
            }
        }
    }

    fun saveThemes(data: QmsThemes) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(QmsThemesBd::class.java).equalTo("userId", data.user.id).findAll()
                .deleteAllFromRealm()
            realmTr.copyToRealmOrUpdate(data.toDb())
        }
        getOrCreateThemesRelay(data.user.id).value = getThemes(data.user.id)
    }

    private fun getOrCreateThemesRelay(userId: Int): MutableStateFlow<QmsThemes?> {
        return themesFlows[userId] ?: MutableStateFlow<QmsThemes?>(null).also {
            themesFlows[userId] = it
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
        themes = themes.toRealmList { it.toDb() })
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

fun <T, R> Iterable<T>.toRealmList(block: (T) -> R): RealmList<R> {
    val list = RealmList<R>()
    forEach { list.add(block.invoke(it)) }
    return list
}