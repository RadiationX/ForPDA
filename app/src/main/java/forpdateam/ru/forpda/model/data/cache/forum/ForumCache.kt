package forpdateam.ru.forpda.model.data.cache.forum

import forpdateam.ru.forpda.common.realm.wrapper.RealmWrapper
import forpdateam.ru.forpda.common.realm.wrapper.query
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat

class ForumCache(
    private val realm: RealmWrapper
) {

    suspend fun getItems(): List<ForumItemFlat> {
        return realm
            .query<ForumItemFlatBd>()
            .mapAll { it.toDomain() }
    }

    suspend fun saveItems(items: List<ForumItemFlat>) {
        realm.write {
            delete(ForumItemFlatBd::class)
            upsertAll(items.map { it.toDb() })
        }
    }
}

fun ForumItemFlatBd.toDomain(): ForumItemFlat {
    return ForumItemFlat(id, parentId, level, title)
}

fun ForumItemFlat.toDb(): ForumItemFlatBd {
    return ForumItemFlatBd(id, parentId, level, title)
}