package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.model.interactors.events.models.NotificationEvent
import forpdateam.ru.forpda.model.interactors.events.models.NotificationId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class NotificationEventsHandler {

    private val newEvents = MutableSharedFlow<NotificationEvent>()

    private val cancelIds = MutableSharedFlow<NotificationId>()

    fun observeNewEvents(): Flow<NotificationEvent> = newEvents

    fun observeCancelIds(): Flow<NotificationId> = cancelIds

    suspend fun handle(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.Forum -> when (event.type) {
                is WebSocketEvent.Forum.Type.New -> {
                    newEvents.emit(NotificationEvent.Forum(NotificationId.Forum(event.forumId)))
                }

                is WebSocketEvent.Forum.Type.Read -> {
                    cancelIds.emit(NotificationId.Forum(event.forumId))
                }
            }

            is WebSocketEvent.Qms -> when (event.type) {
                is WebSocketEvent.Qms.Type.New -> Unit
                is WebSocketEvent.Qms.Type.Read -> {
                    cancelIds.emit(NotificationId.Qms(event.themeId))
                }

                is WebSocketEvent.Qms.Type.ReadAll -> {
                    cancelIds.emit(NotificationId.Qms(event.themeId))
                }

                WebSocketEvent.Qms.Type.Typing -> Unit
                WebSocketEvent.Qms.Type.Uploading -> Unit
            }

            is WebSocketEvent.Site -> when (event.type) {
                is WebSocketEvent.Site.Type.Mention -> {
                    newEvents.emit(NotificationEvent.SiteMention(NotificationId.SiteMention(event.articleId), event.type.commentId))
                }

                is WebSocketEvent.Site.Type.Read -> {
                    cancelIds.emit(NotificationId.SiteMention(event.articleId))
                }
            }

            is WebSocketEvent.Topic -> when (event.type) {
                is WebSocketEvent.Topic.Type.HatUpdate -> Unit
                is WebSocketEvent.Topic.Type.Mention -> {
                    newEvents.emit(NotificationEvent.TopicMention(NotificationId.TopicMention(event.topicId), event.type.postId))
                }

                is WebSocketEvent.Topic.Type.New -> Unit
                is WebSocketEvent.Topic.Type.Read -> {
                    cancelIds.emit(NotificationId.Favorite(event.topicId))
                    cancelIds.emit(NotificationId.TopicMention(event.topicId))
                }
            }
        }
    }

    suspend fun handle(diff: InspectorDiff<InspectorItem.Favorite>) {
        diff.diff.forEach { diffItem ->
            when (diffItem) {
                is InspectorDiff.Item.New -> {
                    val event = NotificationEvent.Favorite(
                        id = NotificationId.Favorite(diffItem.item.topicId),
                        data = diffItem.item
                    )
                    newEvents.emit(event)
                }

                is InspectorDiff.Item.Read -> {
                    cancelIds.emit(NotificationId.Favorite(diffItem.item.topicId))
                }

                is InspectorDiff.Item.Same -> Unit
            }
        }
    }

    suspend fun handle(diff: InspectorDiff<InspectorItem.Qms>) {
        diff.diff.forEach { diffItem ->
            when (diffItem) {
                is InspectorDiff.Item.New -> {
                    val event = NotificationEvent.Qms(
                        id = NotificationId.Qms(diffItem.item.themeId),
                        data = diffItem.item
                    )
                    newEvents.emit(event)
                }

                is InspectorDiff.Item.Read -> {
                    cancelIds.emit(NotificationId.Qms(diffItem.item.themeId))
                }

                is InspectorDiff.Item.Same -> Unit
            }
        }
    }
}