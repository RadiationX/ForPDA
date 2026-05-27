package forpdateam.ru.forpda.model.interactors.events.models

import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId

sealed interface NotificationId {
    data class Favorite(val topicId: TopicId) : NotificationId
    data class TopicMention(val topicId: TopicId) : NotificationId
    data class Qms(val themeId: QmsThreadId) : NotificationId
    data class SiteMention(val articleId: ArticleId) : NotificationId
    data class Forum(val forumId: ForumId) : NotificationId
}