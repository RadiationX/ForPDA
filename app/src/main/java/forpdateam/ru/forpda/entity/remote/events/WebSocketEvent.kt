package forpdateam.ru.forpda.entity.remote.events

import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId

sealed interface WebSocketEvent {

    data class Topic(
        val type: Type,
        val topicId: TopicId,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class New(val postTimestamp: Long) : Type
            data class Read(val postTimestamp: Long) : Type
            data class Mention(val postId: PostId) : Type
            data class HatUpdate(val postTimestamp: Long) : Type
        }
    }

    data class Site(
        val type: Type,
        val articleId: ArticleId,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class Mention(val commentId: CommentId) : Type
            data class Read(val commentId: CommentId) : Type
        }
    }

    data class Qms(
        val type: Type,
        val themeId: QmsThreadId,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class New(val messageId: QmsMessageId) : Type
            data class Read(val messageId: QmsMessageId) : Type
            data class ReadAll(val messageId: QmsMessageId) : Type
            data object Typing : Type
            data object Uploading : Type
        }
    }

    data class Forum(
        val type: Type,
        val forumId: ForumId,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class New(val postTimestamp: Long) : Type
            data class Read(val readTimestamp: Long) : Type
        }
    }
}