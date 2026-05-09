package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.model.repository.inspector.InspectorRepository

class NotificationEventsHandler(
    private val inspectorRepository: InspectorRepository,
) {



    fun handle(event: WebSocketEvent) {
    }

}