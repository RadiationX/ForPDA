package forpdateam.ru.forpda.model.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class CrossScreenInteractor @Inject constructor() {

    private val announceFlow = MutableSharedFlow<Int>()
    private val articleflow = MutableSharedFlow<Int>()
    private val deviceflow = MutableSharedFlow<Int>()
    private val profileflow = MutableSharedFlow<Int>()
    private val chatflow = MutableSharedFlow<Int>()
    private val topicflow = MutableSharedFlow<Int>()

    fun observeAnnounce(): Flow<Int> = announceFlow
    fun observeArticle(): Flow<Int> = articleflow
    fun observeDevice(): Flow<Int> = deviceflow
    fun observeProfile(): Flow<Int> = profileflow
    fun observeChat(): Flow<Int> = chatflow
    fun observeTopic(): Flow<Int> = topicflow

    suspend fun onLoadAnnounce(id: Int) = announceFlow.emit(id)
    suspend fun onLoadArticle(id: Int) = articleflow.emit(id)
    suspend fun onLoadDevice(id: Int) = deviceflow.emit(id)
    suspend fun onLoadProfile(id: Int) = profileflow.emit(id)
    suspend fun onLoadChat(id: Int) = chatflow.emit(id)
    suspend fun onLoadTopic(id: Int) = topicflow.emit(id)

}