package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.CommentId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.17.
 */
class WebSocketEventParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.WebSocket

    fun parseWebSocketEvent(message: String): WebSocketEvent? {
        return patternProvider
            .getRegexParser(scope.scope, scope.event)
            .mapOnce(message) { match ->
                val sourceId = match.require(4).toInt()
                val type = match.require(5).toInt()
                val typeParam = match.require(6).toLong()
                when (match.require(3)) {
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
            SRC_TYPE_MENTION -> WebSocketEvent.Topic.Type.Mention(postId = PostId(typeParam.toInt()))
            SRC_TYPE_HAT_UPDATE -> WebSocketEvent.Topic.Type.HatUpdate(postTimestamp = typeParam * 1000L)
            else -> null
        } ?: return null
        return WebSocketEvent.Topic(
            type = type,
            topicId = TopicId(sourceId),
            timeStamp = System.currentTimeMillis()
        )
    }


    private fun createWebSocketSite(sourceId: Int, srcType: Int, typeParam: Long): WebSocketEvent.Site? {
        val type = when (srcType) {
            SRC_TYPE_MENTION -> WebSocketEvent.Site.Type.Mention(commentId = CommentId(typeParam.toInt()))
            SRC_TYPE_READ -> WebSocketEvent.Site.Type.Read(commentId = CommentId(typeParam.toInt()))
            else -> null
        } ?: return null
        return WebSocketEvent.Site(
            type = type,
            articleId = ArticleId(sourceId),
            timeStamp = System.currentTimeMillis()
        )
    }

    private fun createWebSocketQms(sourceId: Int, srcType: Int, typeParam: Long): WebSocketEvent.Qms? {
        val type = when (srcType) {
            SRC_TYPE_NEW -> WebSocketEvent.Qms.Type.New(messageId = QmsMessageId(typeParam.toInt()))
            SRC_TYPE_READ -> WebSocketEvent.Qms.Type.Read(messageId = QmsMessageId(typeParam.toInt()))
            SRC_TYPE_QMS_FULL_READ -> WebSocketEvent.Qms.Type.ReadAll(messageId = QmsMessageId(typeParam.toInt()))
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
            themeId = QmsThreadId(sourceId),
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
            forumId = ForumId(sourceId),
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
    }
}
