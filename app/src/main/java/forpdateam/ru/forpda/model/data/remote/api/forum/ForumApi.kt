package forpdateam.ru.forpda.model.data.remote.api.forum

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.forum.Announce
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 15.02.17.
 */

class ForumApi @Inject constructor(
    private val webClient: WebClient,
    private val forumParser: ForumParser
) {

    suspend fun getForums(): List<ForumItemFlat> {
        val response = webClient.request(ApiRequest.Forum.Forums.GetAllForums)
        return forumParser.parseForums(response.body)
    }

    suspend fun getRules(): ForumRules {
        val response = webClient.request(ApiRequest.Forum.Forums.GetRules)
        return forumParser.parseRules(response.body)
    }

    suspend fun getAnnounce(id: Int, forumId: Int): Announce {
        val response = webClient.request(ApiRequest.Forum.Forums.GetAnnounce(forumId, id))
        return forumParser.parseAnnounce(response.body)
    }

    suspend fun markAllRead() {
        webClient.request(ApiRequest.Forum.Forums.MarkAllRead)
    }

    suspend fun markRead(id: Int) {
        webClient.request(ApiRequest.Forum.Forums.MarkRead(id))
    }
}
