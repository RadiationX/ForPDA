package forpdateam.ru.forpda.model.data.remote.api.theme

import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Created by radiationx on 04.08.16.
 */
class ThemeApi(
        private val webClient: IWebClient,
        private val themeParser: ThemeParser
) {

    fun getTheme(url: String, hatOpen: Boolean, pollOpen: Boolean): ThemePage {
        val response = webClient.get(url)
        val redirectUrl: String = response.redirect ?: url
        return themeParser.parsePage(response.body, redirectUrl, hatOpen, pollOpen)
    }

    fun reportPost(topicId: Int, postId: Int, message: String): Boolean {
        val request = NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=report&send=1&t=$topicId&p=$postId")
                .formHeader("message", URLEncoder.encode(message, "windows-1251"), true)
                .build()
        val response = webClient.request(request)
        val p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE)
        val m = p.matcher(response.body)
        if (m.find()) {
            throw Exception("Ошибка отправки жалобы: " + m.group(1))
        }
        return true
    }

    fun deletePost(postId: Int): Boolean {
        val url = "https://4pda.ru/forum/index.php?act=zmod&auth_key=${webClient.authKey}&code=postchoice&tact=delete&selectedpids=$postId"
        val response = webClient.request(NetworkRequest.Builder().url(url).xhrHeader().build())
        val body = response.body
        if (body != "ok") {
            throw Exception("Ошибка изменения репутации поста")
        }
        return true
    }

    fun votePost(postId: Int, type: Boolean): String {
        val response = webClient.get("https://4pda.ru/forum/zka.php?i=$postId&v=${if (type) "1" else "-1"}")
        var result: String? = null

        val alreadyVote = "Ошибка: Вы уже голосовали за это сообщение"

        val m = Pattern.compile("ok:\\s*?((?:\\+|\\-)?\\d+)").matcher(response.body)
        if (m.find()) {
            val code = m.group(1).toInt()
            when (code) {
                0 -> result = alreadyVote
                1 -> result = "Репутация поста повышена"
                -1 -> result = "Репутация поста понижена"
            }
        }
        if (response.body == "evote") {
            result = alreadyVote
        }
        if (result == null) {
            throw Exception("Ошибка изменения репутации поста")
        }
        return result
    }

    companion object {
        val elemToScrollPattern = Pattern.compile("(?:anchor=|#)([^&\\n\\=\\?\\.\\#]*)")
        val attachImagesPattern = Pattern.compile("(4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/[^\"']*?\\.(?:jpe?g|png|gif|bmp))\"?(?:[^>]*?title=\"([^\"']*?\\.(?:jpe?g|png|gif|bmp)) - [^\"']*?\")?")
    }
}
