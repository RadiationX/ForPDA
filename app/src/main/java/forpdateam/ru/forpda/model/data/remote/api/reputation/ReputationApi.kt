package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.entity.remote.reputation.RepArgs
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.util.regex.Pattern

/**
 * Created by radiationx on 20.03.17.
 */

class ReputationApi(
    private val webClient: IWebClient,
    private val reputationParser: ReputationParser
) {

    fun getReputation(userId: Int, mode: String, sort: String, st: Int): RepData {
        val response =
            webClient.get("https://4pda.to/forum/index.php?act=rep&view=history&mid=$userId&mode=$mode&order=$sort&st=$st")
        return reputationParser.parse(response.body)
    }

    fun editReputation(postId: Int, userId: Int, type: Boolean, message: String): Boolean {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php")
            .formHeader("act", "rep")
            .formHeader("mid", userId.toString())
            .formHeader("type", if (type) "add" else "minus")
            .formHeader("message", message)
        if (postId > 0) {
            builder.formHeader("p", postId.toString())
        }
        webClient.request(builder.build())
        return true
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
