package forpdateam.ru.forpda.model.interactors.events

import forpdateam.ru.forpda.model.data.remote.api.events.WebSocketEventsApi
import forpdateam.ru.forpda.model.interactors.events.handlers.CountersEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.FavoritesEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.NotificationEventsHandler
import forpdateam.ru.forpda.model.interactors.events.handlers.QmsEventsHandler
import forpdateam.ru.forpda.model.repository.inspector.InspectorRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class EventsController(
    private val webSocketEventsApi: WebSocketEventsApi,
    private val countersEventsHandler: CountersEventsHandler,
    private val favoritesEventsHandler: FavoritesEventsHandler,
    private val qmsEventsHandler: QmsEventsHandler,
    private val notificationEventsHandler: NotificationEventsHandler,
    private val inspectorRepository: InspectorRepository,
) {

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
    }

}