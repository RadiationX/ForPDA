package forpdateam.ru.forpda.model.data.cache.qms

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import io.reactivex.Observable
import io.realm.Realm
import java.lang.Exception

class QmsCache {

    private val contactsRelay = BehaviorRelay.create<List<QmsContact>>()
    private val themesRelays = mutableMapOf<Int, BehaviorRelay<QmsThemes>>()

    fun observeContacts(): Observable<List<QmsContact>> = contactsRelay.hide()
    fun observeThemes(userId: Int): Observable<QmsThemes> = getOrCreateThemesRelay(userId).hide()

    fun getContacts(): List<QmsContact> = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsContactBd::class.java).findAll().map { QmsContact(it) }
    }.also {
        if (!contactsRelay.hasValue()) {
            contactsRelay.accept(it)
        }
    }

    fun saveContacts(items: List<QmsContact>) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.delete(QmsContactBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { QmsContactBd(it) })
        }
        contactsRelay.accept(getContacts())
    }

    fun updateContact(item:QmsContact) = Realm.getDefaultInstance().use { realm->
        realm.executeTransaction {realmTr->
            realmTr.copyToRealmOrUpdate(QmsContactBd(item))
        }
        if (contactsRelay.hasValue()) {
            realm.where(QmsContactBd::class.java)
                    .equalTo("id", item.id)
                    .findFirst()
                    ?.also { newItem ->
                        val currentItems = contactsRelay.value!!.toMutableList()
                        val index = currentItems.indexOfFirst { newItem.id == it.id }
                        if (index == -1) {
                            contactsRelay.accept(getContacts())
                        } else {
                            currentItems[index] = QmsContact(newItem)
                            contactsRelay.accept(currentItems)
                        }
                    }
        }
    }


    fun getThemes(userId: Int): QmsThemes = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsThemesBd::class.java).equalTo("userId", userId).findAll().last()?.let { result ->
            QmsThemes(result).also { themes ->
                themes.themes.addAll(result.themes.map {
                    QmsTheme(it).also { theme ->
                        theme.nick = themes.nick
                        theme.userId = themes.userId
                    }
                })
            }
        } ?: throw Exception("Not found by userId=$userId")
    }.also { themes ->
        getOrCreateThemesRelay(userId).also {
            if (!it.hasValue()) {
                it.accept(themes)
            }
        }
    }

    fun getAllThemes(): List<QmsThemes> = Realm.getDefaultInstance().use { realm ->
        realm.where(QmsThemesBd::class.java).findAll().map { themesDb ->
            QmsThemes(themesDb).also { themes ->
                themes.themes.addAll(themesDb.themes.map {
                    QmsTheme(it).also { theme ->
                        theme.nick = themes.nick
                        theme.userId = themes.userId
                    }
                })
            }
        }
    }.also { themesList ->
        themesList.forEach { themes ->
            getOrCreateThemesRelay(themes.userId).also {
                if (!it.hasValue()) {
                    it.accept(themes)
                }
            }
        }
    }

    fun saveThemes(data: QmsThemes) = Realm.getDefaultInstance().use { realm ->
        realm.executeTransaction { realmTr ->
            realmTr.where(QmsThemesBd::class.java).equalTo("userId", data.userId).findAll().deleteAllFromRealm()
            realmTr.copyToRealmOrUpdate(QmsThemesBd(data))
        }
        getOrCreateThemesRelay(data.userId).accept(getThemes(data.userId))
    }

    private fun getOrCreateThemesRelay(userId: Int): BehaviorRelay<QmsThemes> = themesRelays[userId]
            ?: BehaviorRelay.create<QmsThemes>().also {
                themesRelays[userId] = it
            }


}