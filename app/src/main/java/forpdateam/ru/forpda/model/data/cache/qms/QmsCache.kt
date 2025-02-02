package forpdateam.ru.forpda.model.data.cache.qms

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmList

class QmsCache {

    private val contactsRelay = BehaviorRelay.create<List<QmsContact>>()
    private val themesRelays = mutableMapOf<Int, BehaviorRelay<QmsThemes>>()

    fun observeContacts(): Observable<List<QmsContact>> = contactsRelay.hide()
    fun observeThemes(userId: Int): Observable<QmsThemes> = getOrCreateThemesRelay(userId).hide()

    fun getContacts(): List<QmsContact> = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsContactBd::class.java).findAll().map { it.toDomain() }
    }.also {
        if (!contactsRelay.hasValue()) {
            contactsRelay.accept(it)
        }
    }

    fun saveContacts(items: List<QmsContact>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(QmsContactBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { it.toDb() })
        }
        contactsRelay.accept(getContacts())
    }

    fun updateContact(item: QmsContact) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.copyToRealmOrUpdate(item.toDb())
        }
        if (contactsRelay.hasValue()) {
            realm.where(QmsContactBd::class.java)
                .equalTo("id", item.user.id)
                .findFirst()
                ?.also { newItem ->
                    val currentItems = contactsRelay.value!!.toMutableList()
                    val index = currentItems.indexOfFirst { newItem.id == it.user.id }
                    if (index == -1) {
                        contactsRelay.accept(getContacts())
                    } else {
                        currentItems[index] = newItem.toDomain()
                        contactsRelay.accept(currentItems)
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
            if (!it.hasValue()) {
                it.accept(themes)
            }
        }
    }

    fun getAllThemes(): List<QmsThemes> = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsThemesBd::class.java).findAll().map { themesDb ->
            themesDb.toDomain()
        }
    }.onEach { themes ->
        getOrCreateThemesRelay(themes.user.id).also {
            if (!it.hasValue()) {
                it.accept(themes)
            }
        }
    }

    fun saveThemes(data: QmsThemes) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(QmsThemesBd::class.java).equalTo("userId", data.user.id).findAll()
                .deleteAllFromRealm()
            realmTr.copyToRealmOrUpdate(data.toDb())
        }
        getOrCreateThemesRelay(data.user.id).accept(getThemes(data.user.id))
    }

    private fun getOrCreateThemesRelay(userId: Int): BehaviorRelay<QmsThemes> = themesRelays[userId]
        ?: BehaviorRelay.create<QmsThemes>().also {
            themesRelays[userId] = it
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