package forpdateam.ru.forpda.model.repository.theme

import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.history.HistoryCache
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Link
import ru.radiationx.links.parser.LinkTransformer
import javax.inject.Inject

/**
 * Created by radiationx on 15.03.18.
 */

class ThemeRepository @Inject constructor(
    private val themeApi: ThemeApi,
    private val historyCache: HistoryCache,
    private val forumUsersCache: ForumUsersCache,
    private val linkTransformer: LinkTransformer
) {

    suspend fun getTheme(link: Link.Board.Topic, hatOpen: Boolean, pollOpen: Boolean): ThemePage {
        return themeApi.getTheme(link, hatOpen, pollOpen).also {
            val forumUsers = it.posts.map { it.post.user }
            forumUsersCache.savePostUsers(forumUsers)
            historyCache.add(it.id.id, linkTransformer.build(it.link).toString(), it.title)
        }
    }

    suspend fun reportPost(topicId: TopicId, postId: PostId, message: String) {
        themeApi.reportPost(topicId, postId, message)
    }

    suspend fun deletePost(postId: PostId) {
        themeApi.deletePost(postId)
    }

    suspend fun votePost(postId: PostId, type: Boolean): String {
        return themeApi.votePost(postId, type)
    }
}
