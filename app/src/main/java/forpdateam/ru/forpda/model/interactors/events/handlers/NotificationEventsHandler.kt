package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.repository.inspector.InspectorRepository

class NotificationEventsHandler(
    private val inspectorRepository: InspectorRepository,
) {


    fun handle(event: WebSocketEvent) {
    }


    private sealed interface NewWebSocketEvent {
        data class FavoriteNew(val topicId: Int, val postId: Int) : NewWebSocketEvent
        data class QmsNew(val themeId: Int, val messageId: Int) : NewWebSocketEvent
    }


    sealed interface NotificationEvent {
        data class FavoritesNew(val topicId: Int, val postId: Int, val user: User, val title: String)
        data class FavoritesMention(val topicId: Int, val postId: Int, val user: User, val title: String)
        data class QmsNew(val themeId: Int, val messageId: Int, val user: User, val title: String)
        data object SiteMention
    }

}