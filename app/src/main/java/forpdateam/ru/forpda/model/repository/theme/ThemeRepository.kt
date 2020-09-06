package forpdateam.ru.forpda.model.repository.theme

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 15.03.18.
 */

class ThemeRepository(
        private val schedulers: SchedulersProvider,
        private val themeApi: ThemeApi,
        private val historyCache: HistoryCache,
        private val forumUsersCache: ForumUsersCache
) : BaseRepository(schedulers) {

    fun getTheme(url: String, withHtml: Boolean, hatOpen: Boolean, pollOpen: Boolean): Single<ThemePage> = Single
            .fromCallable { themeApi.getTheme(url, hatOpen, pollOpen) }
            .doOnSuccess {
                saveUsers(it)
                historyCache.add(it.id, it.url, it.title)
            }
            .runInIoToUi()

    fun reportPost(themeId: Int, postId: Int, message: String): Single<Boolean> = Single
            .fromCallable { themeApi.reportPost(themeId, postId, message) }
            .runInIoToUi()

    fun deletePost(postId: Int): Single<Boolean> = Single
            .fromCallable { themeApi.deletePost(postId) }
            .runInIoToUi()

    fun votePost(postId: Int, type: Boolean): Single<String> = Single
            .fromCallable { themeApi.votePost(postId, type) }
            .runInIoToUi()

    private fun saveUsers(page: ThemePage) {
        val forumUsers = page.posts.map { post ->
            ForumUser().apply {
                id = post.userId
                nick = post.nick
                avatar = post.avatar
            }
        }
        forumUsersCache.saveUsers(forumUsers)
    }
}
