package forpdateam.ru.forpda.model.repository.forum

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.data.cache.forum.ForumCache
import forpdateam.ru.forpda.model.data.remote.api.forum.ForumApi
import javax.inject.Inject

/**
 * Created by radiationx on 03.01.18.
 */

class ForumRepository @Inject constructor(
    private val forumApi: ForumApi,
    private val forumCache: ForumCache
) {

    suspend fun getForums(): List<ForumItemFlat> {
        return forumApi.getForums().also {
            forumCache.saveItems(it)
        }
    }

    suspend fun getCache(): List<ForumItemFlat> {
        return forumCache.getItems()
    }

    suspend fun markAllRead() {
        forumApi.markAllRead()
    }

    suspend fun markRead(id: Int) {
        forumApi.markRead(id)
    }

    suspend fun getRules(): ForumRules {
        return forumApi.getRules()
    }

    suspend fun getAnnounce(id: Int, forumId: Int): Announce {
        return forumApi.getAnnounce(id, forumId)
    }
}
