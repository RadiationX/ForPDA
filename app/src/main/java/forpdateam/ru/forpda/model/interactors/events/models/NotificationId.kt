package forpdateam.ru.forpda.model.interactors.events.models

sealed interface NotificationId {
    data class Favorite(val topicId: Int) : NotificationId
    data class TopicMention(val topicId: Int) : NotificationId
    data class Qms(val themeId: Int) : NotificationId
    data class SiteMention(val articleId: Int) : NotificationId
    data class Forum(val forumId: Int) : NotificationId
}