package forpdateam.ru.forpda.model.data.cache.forum

import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import io.realm.Realm

class ForumCache {

    fun getItems() = Realm.getDefaultInstance().use {
        it.where(ForumItemFlatBd::class.java).findAll().map { ForumItemFlat(it) }
    }

    fun saveItems(items: List<ForumItemFlatBd>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.delete(ForumItemFlatBd::class.java)
            realmTr.copyToRealmOrUpdate(items)
        }
    }

}