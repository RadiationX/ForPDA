package forpdateam.ru.forpda.model.interactors.events

import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.interactors.events.handlers.CountersEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.FavoritesEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.NotificationEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.QmsEventsHandler
import forpdateam.ru.forpda.model.interactors.events.models.InspectorTrigger
import forpdateam.ru.forpda.model.interactors.events.models.NotificationEvent
import forpdateam.ru.forpda.model.interactors.events.models.NotificationId
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.repository.events.WebSocketEventsRepository
import forpdateam.ru.forpda.model.repository.inspector.InspectorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class EventsController(
    private val webSocketEventsRepository: WebSocketEventsRepository,
    private val countersEventsHandler: CountersEventsHandler,
    private val favoritesEventsHandler: FavoritesEventsHandler,
    private val qmsEventsHandler: QmsEventsHandler,
    private val notificationEventsHandler: NotificationEventsHandler,
    private val inspectorRepository: InspectorRepository,
    private val notificationPreferencesHolder: NotificationPreferencesHolder,
    private val notificationEventSender: NotificationEventSender
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var started = false

    fun start() {
        if (started) {
            return
        }
        started = true
        webSocketEventsRepository
            .observeEvents()
            .onEach {
                countersEventsHandler.handle(it)
                favoritesEventsHandler.handle(it)
                qmsEventsHandler.handle(it)
                notificationEventsHandler.handle(it)
            }
            .launchIn(coroutineScope)

        notificationEventsHandler
            .observeTriggers()
            .onEach { processInspector(listOf(it)) }
            .launchIn(coroutineScope)

        notificationEventsHandler
            .observeNewEvents()
            .onEach { notificationEventSender.send(it) }
            .launchIn(coroutineScope)

        notificationEventsHandler
            .observeCancelIds()
            .onEach { notificationEventSender.cancel(it) }
            .launchIn(coroutineScope)

        notificationPreferencesHolder
            .mainPeriodDuration
            .flatMapLatest { timerPeriod ->
                flow {
                    while (true) {
                        emit(Unit)
                        delay(timerPeriod)
                    }
                }
            }
            .onEach {
                checkEvents()
            }
            .launchIn(coroutineScope)
    }

    fun stop() {
        coroutineScope.coroutineContext.cancelChildren()
        started = false
    }

    suspend fun checkEvents() {
        processInspector(InspectorTrigger.entries.toList())
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