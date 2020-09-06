package forpdateam.ru.forpda.entity.app

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent

/**
 * Created by radiationx on 26.09.17.
 */

data class TabNotification(
        val source: NotificationEvent.Source,
        val type: NotificationEvent.Type,
        val event: NotificationEvent,
        val isWebSocket: Boolean,
        val loadedEvents: List<NotificationEvent> = emptyList(),
        val newEvents: List<NotificationEvent> = emptyList()
)
