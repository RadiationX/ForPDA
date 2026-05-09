package forpdateam.ru.forpda.model.interactors.events

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.events.WebSocketEventsApi
import forpdateam.ru.forpda.model.interactors.events.handlers.CountersEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.FavoritesEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.NotificationEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.QmsEventsHandler
import forpdateam.ru.forpda.model.interactors.events.models.InspectorTrigger
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.repository.inspector.InspectorRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class EventsController(
    private val webSocketEventsApi: WebSocketEventsApi,
    private val countersEventsHandler: CountersEventsHandler,
    private val favoritesEventsHandler: FavoritesEventsHandler,
    private val qmsEventsHandler: QmsEventsHandler,
    private val notificationEventsHandler: NotificationEventsHandler,
    private val inspectorRepository: InspectorRepository,
    private val notificationPreferencesHolder: NotificationPreferencesHolder
) {

    private var checkTimerJob: Job? = null

    fun observeWebSocketEvents(): Flow<WebSocketEvent> = webSocketEventsApi.observeEvents()

    fun kek() {
        webSocketEventsApi
            .observeEvents()
            .onEach {
                countersEventsHandler.handle(it)
                favoritesEventsHandler.handle(it)
                qmsEventsHandler.handle(it)
                notificationEventsHandler.handle(it)
            }
            .launchIn(GlobalScope)

        notificationEventsHandler
            .observeTriggers()
            .onEach {
                processInspector(listOf(it))
            }
            .launchIn(GlobalScope)
    }

    private fun resetTimer() {
        cancelTimer()
        checkTimerJob = notificationPreferencesHolder
            .observeMainLimit()
            .flatMapLatest { timerPeriod ->
                flow {
                    while (true) {
                        emit(Unit)
                        delay(timerPeriod)
                    }
                }
            }
            .onEach {
                processInspector(InspectorTrigger.entries.toList())
            }
            .launchIn(GlobalScope)
    }

    private fun cancelTimer() {
        checkTimerJob?.cancel()
        checkTimerJob = null
    }

    private suspend fun processInspector(triggers: List<InspectorTrigger>) {
        coroutineScope {
            triggers.map { trigger ->
                async {
                    when (trigger) {
                        InspectorTrigger.Favorites -> processFavorites()
                        InspectorTrigger.Qms -> processQms()
                        InspectorTrigger.Mentions -> processMentions()
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun processFavorites() {
        coRunCatching {
            inspectorRepository.getFavoritesDiff()
        }.onSuccess {
            countersEventsHandler.handle(it)
            favoritesEventsHandler.handle(it)
            notificationEventsHandler.handle(it)
            inspectorRepository.saveFavorites(it)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private suspend fun processQms() {
        coRunCatching {
            inspectorRepository.getQmsDiff()
        }.onSuccess {
            countersEventsHandler.handle(it)
            qmsEventsHandler.handle(it)
            notificationEventsHandler.handle(it)
            inspectorRepository.saveQms(it)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private suspend fun processMentions() {
        coRunCatching {
            inspectorRepository.getMentionsCount()
        }.onSuccess {
            countersEventsHandler.handle(it)
        }.onFailure {
            it.printStackTrace()
        }
    }


}