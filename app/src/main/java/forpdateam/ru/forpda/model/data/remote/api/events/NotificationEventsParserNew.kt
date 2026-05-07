package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.InspectorEvents
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.fromHtml
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.07.17.
 */
class NotificationEventsParserNew {

    fun parseWebSocketEvent(message: String): WebSocketEvent? {
        return webSocketEventPattern.matcher(message).mapOnce { matcher ->
            //// TODO: 02.10.17 сделать обратку нотификации форума
            val sourceId = matcher.group(4).toInt()
            val messageId = matcher.group(6).toInt()
            val type = matcher.group(5).toInt()
            return when (matcher.group(3)) {
                SRC_SOURCE_THEME -> createWebSocketTheme(type, sourceId, messageId)
                SRC_SOURCE_SITE -> createWebSocketSite(type, sourceId, messageId)
                SRC_SOURCE_QMS -> createWebSocketQms(type, sourceId, messageId)
                SRC_SOURCE_FORUM -> createWebSocketForum(type, sourceId, messageId)
                else -> null
            }
        }
    }

    fun parseFavoritesEvents(response: String): List<InspectorEvents.Theme> {
        return inspectorFavoritesPattern.matcher(response).map { matcher ->
            InspectorEvents.Theme(
                topicId = matcher.group(1).toInt(),
                sourceTitle = fromHtml(matcher.group(2))!!,
                msgCount = matcher.group(3).toInt(),
                user = User.required(
                    id = matcher.group(4).toInt(),
                    nick = fromHtml(matcher.group(5))
                ),
                timeStamp = matcher.group(6).toInt().toLong(),
                lastTimeStamp = matcher.group(7).toInt().toLong(),
                isImportant = matcher.group(8) == "1",
            )
        }
    }

    fun parseQmsEvents(response: String): List<InspectorEvents.Qms> {
        return inspectorQmsPattern.matcher(response).map { matcher ->
            val sourceId = matcher.group(1).toInt()
            var userNick = fromHtml(matcher.group(4))!!
            if (userNick.isEmpty() && sourceId == 0) {
                userNick = "Сообщения 4PDA"
            }
            InspectorEvents.Qms(
                themeId = sourceId,
                sourceTitle = fromHtml(matcher.group(2))!!,
                user = User.required(
                    id = matcher.group(3).toInt(),
                    nick = userNick
                ),
                timeStamp = matcher.group(5).toInt().toLong(),
                msgCount = matcher.group(6).toInt(),
            )
        }
    }

    private fun createWebSocketTheme(srcType: Int, sourceId: Int, messageId: Int): WebSocketEvent.Theme? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.Theme.Type.New
            SRC_TYPE_READ -> WebSocketEvent.Theme.Type.Read
            SRC_TYPE_MENTION -> WebSocketEvent.Theme.Type.Mention
            SRC_TYPE_HAT_UPDATE -> WebSocketEvent.Theme.Type.HatUpdate
            else -> return null
        }
        return WebSocketEvent.Theme(
            type = type,
            topicId = sourceId,
            postId = messageId
        )
    }

    private fun createWebSocketSite(srcType: Int, sourceId: Int, messageId: Int): WebSocketEvent.Site? {
        val type = when (srcType) {
            SRC_TYPE_MENTION -> WebSocketEvent.Site.Type.Mention
            SRC_TYPE_READ -> WebSocketEvent.Site.Type.Read
            else -> return null
        }
        return WebSocketEvent.Site(
            type = type,
            postId = sourceId,
            commentId = messageId
        )
    }

    private fun createWebSocketQms(srcType: Int, sourceId: Int, messageId: Int): WebSocketEvent? {
        if (srcType == SRC_TYPE_QMS_ACTION) {
            return parseWebSocketQmsAction(sourceId, messageId)
        }
        return parseWebSocketQmsMessage(srcType, sourceId, messageId)
    }

    private fun parseWebSocketQmsMessage(srcType: Int, sourceId: Int, messageId: Int): WebSocketEvent.QmsMessage? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.QmsMessage.Type.New
            SRC_TYPE_READ -> WebSocketEvent.QmsMessage.Type.Read
            SRC_TYPE_QMS_FULL_READ -> WebSocketEvent.QmsMessage.Type.FullRead
            else -> return null
        }
        return WebSocketEvent.QmsMessage(
            type = type,
            themeId = sourceId,
            messageId = messageId
        )
    }

    private fun parseWebSocketQmsAction(sourceId: Int, messageId: Int): WebSocketEvent.QmsAction? {
        val type = when (messageId) {
            SRC_QMS_ACTION_TYPING -> WebSocketEvent.QmsAction.Type.Typing
            SRC_QMS_ACTION_UPLOADING -> WebSocketEvent.QmsAction.Type.Uploading
            else -> return null
        }
        return WebSocketEvent.QmsAction(
            type = type,
            themeId = sourceId,
        )
    }

    private fun createWebSocketForum(srcType: Int, sourceId: Int, messageId: Int): WebSocketEvent.Forum? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.Forum.Type.New
            SRC_TYPE_READ -> WebSocketEvent.Forum.Type.Read
            else -> return null
        }
        return WebSocketEvent.Forum(
            type = type,
            topicId = sourceId,
            postId = messageId
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
        private const val SRC_SOURCE_THEME = "t"
        private const val SRC_SOURCE_QMS = "q"
        private const val SRC_SOURCE_FORUM = "f"

        private val inspectorFavoritesPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        private val inspectorQmsPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        private val webSocketEventPattern: Pattern =
            Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]")
    }
}
