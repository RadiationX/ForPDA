package forpdateam.ru.forpda.model.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class CrossScreenInteractor {

    private val announceRelay = MutableSharedFlow<Int>()
    private val articleRelay = MutableSharedFlow<Int>()
    private val deviceRelay = MutableSharedFlow<Int>()
    private val profileRelay = MutableSharedFlow<Int>()
    private val chatRelay = MutableSharedFlow<Int>()
    private val topicRelay = MutableSharedFlow<Int>()

    fun observeAnnounce(): Flow<Int> = announceRelay
    fun observeArticle(): Flow<Int> = articleRelay
    fun observeDevice(): Flow<Int> = deviceRelay
    fun observeProfile(): Flow<Int> = profileRelay
    fun observeChat(): Flow<Int> = chatRelay
    fun observeTopic(): Flow<Int> = topicRelay

    suspend fun onLoadAnnounce(id: Int) = announceRelay.emit(id)
    suspend fun onLoadArticle(id: Int) = articleRelay.emit(id)
    suspend fun onLoadDevice(id: Int) = deviceRelay.emit(id)
    suspend fun onLoadProfile(id: Int) = profileRelay.emit(id)
    suspend fun onLoadChat(id: Int) = chatRelay.emit(id)
    suspend fun onLoadTopic(id: Int) = topicRelay.emit(id)

}