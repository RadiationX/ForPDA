package forpdateam.ru.forpda.model.data.remote.api.forum

import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest

/**
 * Created by radiationx on 15.02.17.
 */

class ForumApi(
    private val webClient: IWebClient,
    private val forumParser: ForumParser
) {

    fun getForums(): List<ForumItemFlat> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=search")
        return forumParser.parseForums(response.body)
    }

    fun getRules(): ForumRules {
        val response = webClient.get("https://4pda.to/forum/index.php?act=boardrules")
        return forumParser.parseRules(response.body)
    }

    fun getAnnounce(id: Int, forumId: Int): Announce {
        val response =
            webClient.get("https://4pda.to/forum/index.php?act=announce&f=$forumId&st=$id")
        return forumParser.parseAnnounce(response.body)
    }

    fun markAllRead(): Any {
        webClient.request(
            NetworkRequest.Builder()
                .url("https://4pda.to/forum/index.php?act=auth&action=markboard").withoutBody()
                .build()
        )
        return Any()
    }

    fun markRead(id: Int): Any {
        webClient.request(
            NetworkRequest.Builder()
                .url("https://4pda.to/forum/index.php?act=auth&action=markforum&f=$id&fromforum=$id")
                .withoutBody().build()
        )
        return Any()
    }
}
