package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.extensions.mapOnce
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.07.17.
 */
class WebSocketEventParser {

    fun parseWebSocketEvent(message: String): WebSocketEvent? {
        return webSocketEventPattern.matcher(message).mapOnce { matcher ->
            //// TODO: 02.10.17 сделать обратку нотификации форума
            val sourceId = matcher.group(4).toInt()
            val type = matcher.group(5).toInt()
            val typeParam = matcher.group(6).toLong()
            return when (matcher.group(3)) {
                SRC_SOURCE_TOPIC -> createWebSocketTopic(sourceId, type, typeParam)
                SRC_SOURCE_SITE -> createWebSocketSite(sourceId, type, typeParam)
                SRC_SOURCE_QMS -> createWebSocketQms(sourceId, type, typeParam)
                SRC_SOURCE_FORUM -> createWebSocketForum(sourceId, type, typeParam)
                else -> null
            }
        }
    }

    private fun createWebSocketTopic(sourceId: Int, srcType: Int, typeParam: Long): WebSocketEvent.Topic? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.Topic.Type.New(postTimestamp = typeParam * 1000L)
            SRC_TYPE_READ -> WebSocketEvent.Topic.Type.Read(postTimestamp = typeParam * 1000L)
            SRC_TYPE_MENTION -> WebSocketEvent.Topic.Type.Mention(postId = typeParam.toInt())
            SRC_TYPE_HAT_UPDATE -> WebSocketEvent.Topic.Type.HatUpdate(postTimestamp = typeParam * 1000L)
            else -> null
        } ?: return null
        return WebSocketEvent.Topic(
            type = type,
            topicId = sourceId,
            timeStamp = System.currentTimeMillis()
        )
    }


    private fun createWebSocketSite(sourceId: Int, srcType: Int, typeParam: Long): WebSocketEvent.Site? {
        val type = when (srcType) {
            SRC_TYPE_MENTION -> WebSocketEvent.Site.Type.Mention(commentId = typeParam.toInt())
            SRC_TYPE_READ -> WebSocketEvent.Site.Type.Read(commentId = typeParam.toInt())
            else -> null
        } ?: return null
        return WebSocketEvent.Site(
            type = type,
            articleId = sourceId,
            timeStamp = System.currentTimeMillis()
        )
    }

    private fun createWebSocketQms(sourceId: Int, srcType: Int, typeParam: Long): WebSocketEvent.Qms? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.Qms.Type.New(messageId = typeParam.toInt())
            SRC_TYPE_READ -> WebSocketEvent.Qms.Type.Read(messageId = typeParam.toInt())
            SRC_TYPE_QMS_FULL_READ -> WebSocketEvent.Qms.Type.ReadAll(messageId = typeParam.toInt())
            SRC_TYPE_QMS_ACTION -> {
                when (typeParam.toInt()) {
                    SRC_QMS_ACTION_TYPING -> WebSocketEvent.Qms.Type.Typing
                    SRC_QMS_ACTION_UPLOADING -> WebSocketEvent.Qms.Type.Uploading
                    else -> null
                }
            }

            else -> null
        } ?: return null
        return WebSocketEvent.Qms(
            type = type,
            themeId = sourceId,
            timeStamp = System.currentTimeMillis()
        )
    }

    private fun createWebSocketForum(sourceId: Int, srcType: Int, typeParam: Long): WebSocketEvent.Forum? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.Forum.Type.New(postTimestamp = typeParam * 1000L)
            SRC_TYPE_READ -> WebSocketEvent.Forum.Type.Read(readTimestamp = typeParam * 1000L)
            else -> return null
        }
        return WebSocketEvent.Forum(
            type = type,
            forumId = sourceId,
            timeStamp = System.currentTimeMillis()
        )
    }

    companion object {
        private const val SRC_TYPE_NEW = 1
        private const val SRC_TYPE_READ = 2
        private const val SRC_TYPE_MENTION = 3
        private const val SRC_TYPE_HAT_UPDATE = 4
        private const val SRC_TYPE_QMS_ACTION = 101
        private const val SRC_TYPE_QMS_FULL_READ = 102

        private const val SRC_QMS_ACTION_TYPING = 0
        private const val SRC_QMS_ACTION_UPLOADING = 1

        private const val SRC_SOURCE_SITE = "s"
        private const val SRC_SOURCE_TOPIC = "t"
        private const val SRC_SOURCE_QMS = "q"
        private const val SRC_SOURCE_FORUM = "f"

        private val webSocketEventPattern: Pattern =
            Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]")
    }
}
