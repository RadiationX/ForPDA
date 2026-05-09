package forpdateam.ru.forpda.entity.remote.events

sealed interface WebSocketEvent {

    data class Topic(
        val type: Type,
        val topicId: Int,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class New(val postTimestamp: Long) : Type
            data class Read(val postTimestamp: Long) : Type
            data class Mention(val postId: Int) : Type
            data class HatUpdate(val postTimestamp: Long) : Type
        }
    }

    data class Site(
        val type: Type,
        val articleId: Int,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class Mention(val commentId: Int) : Type
            data class Read(val commentId: Int) : Type
        }
    }

    data class Qms(
        val type: Type,
        val themeId: Int,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class New(val messageId: Int) : Type
            data class Read(val messageId: Int) : Type
            data class ReadAll(val messageId: Int) : Type
            data object Typing : Type
            data object Uploading : Type
        }
    }

    data class Forum(
        val type: Type,
        val forumId: Int,
        val timeStamp: Long
    ) : WebSocketEvent {

        sealed interface Type {
            data class New(val postTimestamp: Long) : Type
            data class Read(val readTimestamp: Long) : Type
        }
    }
}