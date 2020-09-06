package forpdateam.ru.forpda.model.repository.search

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.search.SearchApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class SearchRepository(
        private val schedulers: SchedulersProvider,
        private val searchApi: SearchApi,
        private val forumUsersCache: ForumUsersCache
) : BaseRepository(schedulers) {

    fun getSearch(settings: SearchSettings): Single<SearchResult> = Single
            .fromCallable { searchApi.getSearch(settings) }
            .doOnSuccess { saveUsers(it) }
            .runInIoToUi()

    private fun saveUsers(page: SearchResult) {
        val forumUsers = page.items.map { post ->
            ForumUser().apply {
                id = post.userId
                nick = post.nick
                avatar = post.avatar
            }
        }
        forumUsersCache.saveUsers(forumUsers)
    }

}
