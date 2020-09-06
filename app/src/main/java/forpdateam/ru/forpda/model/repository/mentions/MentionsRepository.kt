package forpdateam.ru.forpda.model.repository.mentions

import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.mentions.MentionsApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class MentionsRepository(
        private val schedulers: SchedulersProvider,
        private val mentionsApi: MentionsApi
) : BaseRepository(schedulers) {

    fun getMentions(page: Int): Single<MentionsData> = Single
            .fromCallable { mentionsApi.getMentions(page) }
            .runInIoToUi()

}
