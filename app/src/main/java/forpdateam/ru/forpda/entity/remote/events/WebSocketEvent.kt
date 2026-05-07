package forpdateam.ru.forpda.entity.remote.events

sealed interface WebSocketEvent {

    data class Favorite(
        val type: Type,
        val topicId: Int,
        val postId: Int,
    ) : WebSocketEvent {

        enum class Type {
            New,
            Read,
            Mention,
            HatUpdate
        }
    }

    data class Site(
        val type: Type,
        val postId: Int,
        val commentId: Int,
    ) : WebSocketEvent {

        enum class Type {
            Mention,
            Read
        }
    }

    data class QmsMessage(
        val type: Type,
        val themeId: Int,
        val messageId: Int,
    ) : WebSocketEvent {

        enum class Type {
            New,
            Read,
            FullRead
        }
    }

    data class QmsAction(
        val type: Type,
        val themeId: Int,
    ) : WebSocketEvent {

        enum class Type {
            Typing,
            Uploading,
        }
    }

    data class Forum(
        val type: Type,
        val topicId: Int,
        val postId: Int,
    ) : WebSocketEvent {

        enum class Type {
            New,
            Read,
        }
    }
}

fun WebSocketEvent.needNotification(): Boolean {
    return when (this) {
        is WebSocketEvent.Forum -> when (type) {
            WebSocketEvent.Forum.Type.New -> true
            WebSocketEvent.Forum.Type.Read -> false
        }

        is WebSocketEvent.QmsAction -> when (type) {
            WebSocketEvent.QmsAction.Type.Typing -> false
            WebSocketEvent.QmsAction.Type.Uploading -> false
        }

        is WebSocketEvent.QmsMessage -> when (type) {
            WebSocketEvent.QmsMessage.Type.New -> true
            WebSocketEvent.QmsMessage.Type.Read -> false
            WebSocketEvent.QmsMessage.Type.FullRead -> false
        }

        is WebSocketEvent.Site -> when (type) {
            WebSocketEvent.Site.Type.Mention -> true
            WebSocketEvent.Site.Type.Read -> false
        }

        is WebSocketEvent.Favorite -> when (type) {
            WebSocketEvent.Favorite.Type.New -> true
            WebSocketEvent.Favorite.Type.Read -> false
            WebSocketEvent.Favorite.Type.Mention -> true
            WebSocketEvent.Favorite.Type.HatUpdate -> false
        }
    }
}

fun WebSocketEvent.needCancelNotification(): Boolean {
    return when (this) {
        is WebSocketEvent.Forum -> when (type) {
            WebSocketEvent.Forum.Type.New -> false
            WebSocketEvent.Forum.Type.Read -> true
        }

        is WebSocketEvent.QmsAction -> when (type) {
            WebSocketEvent.QmsAction.Type.Typing -> false
            WebSocketEvent.QmsAction.Type.Uploading -> false
        }

        is WebSocketEvent.QmsMessage -> when (type) {
            WebSocketEvent.QmsMessage.Type.New -> false
            WebSocketEvent.QmsMessage.Type.Read -> true
            WebSocketEvent.QmsMessage.Type.FullRead -> true
        }

        is WebSocketEvent.Site -> when (type) {
            WebSocketEvent.Site.Type.Mention -> false
            WebSocketEvent.Site.Type.Read -> true
        }

        is WebSocketEvent.Favorite -> when (type) {
            WebSocketEvent.Favorite.Type.New -> false
            WebSocketEvent.Favorite.Type.Read -> true
            WebSocketEvent.Favorite.Type.Mention -> false
            WebSocketEvent.Favorite.Type.HatUpdate -> false
        }
    }
}