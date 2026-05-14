package forpdateam.ru.forpda.model.repository.theme

import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi

/**
 * Created by radiationx on 15.03.18.
 */

class ThemeRepository(
    private val themeApi: ThemeApi,
    private val historyCache: HistoryCache,
    private val forumUsersCache: ForumUsersCache
) {

    suspend fun getTheme(
        url: String,
        hatOpen: Boolean,
        pollOpen: Boolean
    ): ThemePage {
        return themeApi.getTheme(url, hatOpen, pollOpen).also {
            val forumUsers = it.posts.map { it.post.user }
            forumUsersCache.savePostUsers(forumUsers)
            historyCache.add(it.id, it.url, it.title)
        }
    }

    suspend fun reportPost(themeId: Int, postId: Int, message: String) {
        themeApi.reportPost(themeId, postId, message)
    }

    suspend fun deletePost(postId: Int) {
        themeApi.deletePost(postId)
    }

    suspend fun votePost(postId: Int, type: Boolean): String {
        return themeApi.votePost(postId, type)
    }
}
