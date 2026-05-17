package forpdateam.ru.forpda.model.data.remote.api.theme

import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.net.URLEncoder
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 04.08.16.
 */
class ThemeApi @Inject constructor(
    private val webClient: WebClient,
    private val themeParser: ThemeParser,
    private val authHolder: AuthHolder
) {

    suspend fun getTheme(url: String, hatOpen: Boolean, pollOpen: Boolean): ThemePage {
        val response = webClient.get(url)
        val redirectUrl: String = response.redirect
        return themeParser.parsePage(response.body, redirectUrl, hatOpen, pollOpen)
    }

    suspend fun reportPost(topicId: Int, postId: Int, message: String) {
        val request = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=report&send=1&t=$topicId&p=$postId")
            .formHeader("message", URLEncoder.encode(message, "windows-1251"), true)
            .build()
        val response = webClient.request(request)
        themeParser.parseReportPostError(response.body)?.also {
            throw Exception("Ошибка отправки жалобы: $it")
        }
    }

    suspend fun deletePost(postId: Int) {
        val authKey = authHolder.getAuthKey().orEmpty()
        val url = "https://4pda.to/forum/index.php?act=zmod&auth_key=${authKey}&code=postchoice&tact=delete&selectedpids=$postId"
        val response = webClient.request(NetworkRequest.Builder().url(url).xhrHeader().build())
        if (!themeParser.checkDeletePostSuccess(response.body)) {
            throw Exception("Ошибка удалении поста")
        }
    }

    suspend fun votePost(postId: Int, type: Boolean): String {
        val response = webClient.get("https://4pda.to/forum/zka.php?i=$postId&v=${if (type) "1" else "-1"}")
        val code = themeParser.parseVotePostResult(response.body)
        return when (code) {
            -1 -> "Репутация поста понижена"
            0 -> "Ошибка: Вы уже голосовали за это сообщение"
            1 -> "Репутация поста повышена"
            else -> throw Exception("Ошибка изменения репутации поста")
        }
    }
}
