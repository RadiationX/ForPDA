package forpdateam.ru.forpda.model.interactors.events.models

import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.PostId

sealed interface NotificationEvent {
    data class Favorite(val id: NotificationId.Favorite, val data: InspectorItem.Favorite) : NotificationEvent
    data class TopicMention(val id: NotificationId.TopicMention, val postId: PostId) : NotificationEvent
    data class Qms(val id: NotificationId.Qms, val data: InspectorItem.Qms) : NotificationEvent
    data class SiteMention(val id: NotificationId.SiteMention, val commentId: CommentId) : NotificationEvent
    data class Forum(val id: NotificationId.Forum) : NotificationEvent
}