package forpdateam.ru.forpda.model.repository.search

import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi

/**
 * Created by radiationx on 01.01.18.
 */

class SearchRepository(
    private val searchApi: SearchApi,
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun getSearch(settings: SearchSettings): SearchResult {
        return searchApi.getSearch(settings).also {
            val forumUsers = it.items.filterIsInstance<SearchItem.Post>().map { post ->
                post.post.user
            }
            forumUsersCache.savePostUsers(forumUsers)
        }
    }

}
