package forpdateam.ru.forpda.model.data.remote.api.mentions

import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 21.01.17.
 */

class MentionsApi @Inject constructor(
    private val webClient: WebClient,
    private val mentionsParser: MentionsParser
) {
    suspend fun getMentions(st: Int): MentionsData {
        val response = webClient.get("https://4pda.to/forum/index.php?act=mentions&st=$st")
        return mentionsParser.parse(response.body)
    }
}
