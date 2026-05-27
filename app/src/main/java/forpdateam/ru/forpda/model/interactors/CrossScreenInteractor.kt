package forpdateam.ru.forpda.model.interactors

import android.companion.DeviceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class CrossScreenInteractor @Inject constructor() {

    private val announceFlow = MutableSharedFlow<AnnounceId>()
    private val articleflow = MutableSharedFlow<ArticleId>()
    private val deviceflow = MutableSharedFlow<DeviceId>()
    private val profileflow = MutableSharedFlow<UserId>()
    private val chatflow = MutableSharedFlow<QmsChatId>()
    private val topicflow = MutableSharedFlow<TopicId>()

    fun observeAnnounce(): Flow<AnnounceId> = announceFlow
    fun observeArticle(): Flow<ArticleId> = articleflow
    fun observeDevice(): Flow<DeviceId> = deviceflow
    fun observeProfile(): Flow<UserId> = profileflow
    fun observeChat(): Flow<QmsChatId> = chatflow
    fun observeTopic(): Flow<TopicId> = topicflow

    suspend fun onLoadAnnounce(id: AnnounceId) = announceFlow.emit(id)
    suspend fun onLoadArticle(id: ArticleId) = articleflow.emit(id)
    suspend fun onLoadDevice(id: DeviceId) = deviceflow.emit(id)
    suspend fun onLoadProfile(id: UserId) = profileflow.emit(id)
    suspend fun onLoadChat(id: QmsChatId) = chatflow.emit(id)
    suspend fun onLoadTopic(id: TopicId) = topicflow.emit(id)

}