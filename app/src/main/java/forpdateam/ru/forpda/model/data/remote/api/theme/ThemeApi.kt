package forpdateam.ru.forpda.model.data.remote.api.theme

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.WebClient
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Link
import ru.radiationx.links.parser.LinkTransformer
import javax.inject.Inject

/**
 * Created by radiationx on 04.08.16.
 */
class ThemeApi @Inject constructor(
    private val webClient: WebClient,
    private val themeParser: ThemeParser,
    private val authHolder: AuthHolder,
    private val linkTransformer: LinkTransformer
) {

    suspend fun getTheme(link: Link.Board.Topic, hatOpen: Boolean, pollOpen: Boolean): ThemePage {
        val response = webClient.get(linkTransformer.build(link).toString())
        val redirectUrl: String = response.redirect
        return themeParser.parsePage(response.body, redirectUrl, hatOpen, pollOpen)
    }

    suspend fun reportPost(topicId: TopicId, postId: PostId, message: String) {
        val response = webClient.request(ApiRequest.Forum.Post.Report(topicId, postId, message))
        themeParser.parseReportPostError(response.body)?.also {
            throw Exception("Ошибка отправки жалобы: $it")
        }
    }

    suspend fun deletePost(postId: PostId) {
        val response = webClient.request(ApiRequest.Forum.Post.Delete(postId, authHolder.getAuthKey()))
        if (!themeParser.checkDeletePostSuccess(response.body)) {
            throw Exception("Ошибка удалении поста")
        }
    }

    suspend fun votePost(postId: PostId, type: Boolean): String {
        val value = if (type) "1" else "-1"
        val response = webClient.request(ApiRequest.Forum.Post.Vote(postId, value))
        val code = themeParser.parseVotePostResult(response.body)
        return when (code) {
            -1 -> "Репутация поста понижена"
            0 -> "Ошибка: Вы уже голосовали за это сообщение"
            1 -> "Репутация поста повышена"
            else -> throw Exception("Ошибка изменения репутации поста")
        }
    }
}
