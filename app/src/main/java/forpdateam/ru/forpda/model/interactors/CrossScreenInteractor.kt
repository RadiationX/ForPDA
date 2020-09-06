package forpdateam.ru.forpda.model.interactors

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class CrossScreenInteractor {

    private val announceRelay = PublishRelay.create<Int>()
    private val articleRelay = PublishRelay.create<Int>()
    private val deviceRelay = PublishRelay.create<Int>()
    private val profileRelay = PublishRelay.create<Int>()
    private val chatRelay = PublishRelay.create<Int>()
    private val topicRelay = PublishRelay.create<Int>()

    fun observeAnnounce(): Observable<Int> = announceRelay.hide()
    fun observeArticle(): Observable<Int> = articleRelay.hide()
    fun observeDevice(): Observable<Int> = deviceRelay.hide()
    fun observeProfile(): Observable<Int> = profileRelay.hide()
    fun observeChat(): Observable<Int> = chatRelay.hide()
    fun observeTopic(): Observable<Int> = topicRelay.hide()

    fun onLoadAnnounce(id: Int) = announceRelay.accept(id)
    fun onLoadArticle(id: Int) = articleRelay.accept(id)
    fun onLoadDevice(id: Int) = deviceRelay.accept(id)
    fun onLoadProfile(id: Int) = profileRelay.accept(id)
    fun onLoadChat(id: Int) = chatRelay.accept(id)
    fun onLoadTopic(id: Int) = topicRelay.accept(id)

}