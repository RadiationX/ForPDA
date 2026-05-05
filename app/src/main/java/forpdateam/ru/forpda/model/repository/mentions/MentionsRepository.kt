package forpdateam.ru.forpda.model.repository.mentions

import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsApi

/**
 * Created by radiationx on 01.01.18.
 */

class MentionsRepository(
    private val mentionsApi: MentionsApi
) {

    suspend fun getMentions(page: Int): MentionsData {
        return mentionsApi.getMentions(page)
    }

}
