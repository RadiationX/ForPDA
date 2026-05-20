package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.reputation.RepArgs
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.WebClient
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 20.03.17.
 */

class ReputationApi @Inject constructor(
    private val webClient: WebClient,
    private val reputationParser: ReputationParser
) {

    suspend fun getReputation(userId: Int, mode: String, sort: String, st: Int): RepData {
        val response = webClient.request(ApiRequest.Forum.Reputation.GetPage(userId, mode, sort, st))
        return reputationParser.parse(response.body)
    }

    suspend fun editReputation(postId: Int, userId: Int, type: Boolean, message: String) {
        val type = if (type) "add" else "minus"
        webClient.request(ApiRequest.Forum.Reputation.Edit(postId, userId, type, message))
    }

    companion object {
        const val MODE_TO = "to"
        const val MODE_FROM = "from"
        const val SORT_ASC = "asc"
        const val SORT_DESC = "desc"

        fun fromUrl(url: String): RepArgs {
            return RepArgs(
                userId = Pattern.compile("mid=(\\d+)").matcher(url).requireOnce {
                    it.group(1).toInt()
                },
                initialSt = Pattern.compile("st=(\\d+)").matcher(url).mapOnce {
                    it.group(1).toInt()
                } ?: 0,
                mode = Pattern.compile("mode=([^&]+)").matcher(url).mapOnce {
                    when (it.group(1)) {
                        MODE_FROM -> MODE_FROM
                        MODE_TO -> MODE_TO
                        else -> null
                    }
                } ?: MODE_TO,
                sort = Pattern.compile("order=([^&]+)").matcher(url).mapOnce {
                    when (it.group(1)) {
                        SORT_ASC -> SORT_ASC
                        SORT_DESC -> SORT_DESC
                        else -> null
                    }
                } ?: SORT_DESC
            )
        }
    }
}
