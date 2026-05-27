package forpdateam.ru.forpda.model.repository.mentions

import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsApi
import ru.radiationx.coretypes.PageOffset
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class MentionsRepository @Inject constructor(
    private val mentionsApi: MentionsApi
) {

    suspend fun getMentions(offset: PageOffset): MentionsData {
        return mentionsApi.getMentions(offset)
    }

}
