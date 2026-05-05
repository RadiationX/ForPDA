package forpdateam.ru.forpda.model.data.cache.forum

import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import io.realm.Realm

class ForumCache {

    suspend fun getItems() = Realm.getDefaultInstance().use {
        it.where(ForumItemFlatBd::class.java).findAll().map { it.toDomain() }
    }

    suspend fun saveItems(items: List<ForumItemFlat>) = Realm.getDefaultInstance().use {
        it.executeTransaction { realmTr ->
            realmTr.delete(ForumItemFlatBd::class.java)
            realmTr.copyToRealmOrUpdate(items.map { it.toDb() })
        }
    }
}

fun ForumItemFlatBd.toDomain(): ForumItemFlat {
    return ForumItemFlat(id, parentId, level, title)
}

fun ForumItemFlat.toDb(): ForumItemFlatBd {
    return ForumItemFlatBd(id, parentId, level, title)
}