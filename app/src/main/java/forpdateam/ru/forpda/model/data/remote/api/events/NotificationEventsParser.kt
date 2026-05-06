package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.fromHtml
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.07.17.
 */
class NotificationEventsParser {

    fun parseWebSocketEvent(message: String): NotificationEvent? {
        return webSocketEventPattern.matcher(message).mapOnce { matcher ->
            //// TODO: 02.10.17 сделать обратку нотификации форума
            val source = when (matcher.group(3)) {
                NotificationEvent.SRC_TYPE_THEME -> NotificationEvent.Source.THEME
                NotificationEvent.SRC_TYPE_SITE -> NotificationEvent.Source.SITE
                NotificationEvent.SRC_TYPE_QMS -> NotificationEvent.Source.QMS
                else -> null
            }
            val type = when (matcher.group(5).toInt()) {
                NotificationEvent.SRC_EVENT_NEW -> NotificationEvent.Type.NEW
                NotificationEvent.SRC_EVENT_READ -> NotificationEvent.Type.READ
                NotificationEvent.SRC_EVENT_MENTION -> NotificationEvent.Type.MENTION
                NotificationEvent.SRC_EVENT_HAT_EDITED -> NotificationEvent.Type.HAT_EDITED
                else -> null
            }

            if (source == null || type == null) {
                return@mapOnce null
            }

            return NotificationEvent(
                type = type,
                source = source,
                sourceId = matcher.group(4).toInt(),
                messageId = matcher.group(6).toInt(),
                user = null,
                timeStamp = 0,
                lastTimeStamp = 0,
                msgCount = 0,
                isImportant = false,
                sourceTitle = "",
                sourceEventText = null,
            )
        }
    }

    fun parseFavoritesEvents(response: String): List<NotificationEvent> {
        return inspectorFavoritesPattern.matcher(response).map { matcher ->
            NotificationEvent(
                type = NotificationEvent.Type.NEW,
                source = NotificationEvent.Source.THEME,
                sourceEventText = matcher.group(),
                sourceId = matcher.group(1).toInt(),
                sourceTitle = fromHtml(matcher.group(2))!!,
                msgCount = matcher.group(3).toInt(),
                user = User.required(
                    id = matcher.group(4).toInt(),
                    nick = fromHtml(matcher.group(5))
                ),
                timeStamp = matcher.group(6).toInt().toLong(),
                lastTimeStamp = matcher.group(7).toInt().toLong(),
                isImportant = matcher.group(8) == "1",
                messageId = 0
            )
        }
    }

    fun parseQmsEvents(response: String): List<NotificationEvent> {
        return inspectorQmsPattern.matcher(response).map { matcher ->
            val sourceId = matcher.group(1).toInt()
            var userNick = fromHtml(matcher.group(4))!!
            if (userNick.isEmpty() && sourceId == 0) {
                userNick = "Сообщения 4PDA"
            }
            NotificationEvent(
                type = NotificationEvent.Type.NEW,
                source = NotificationEvent.Source.QMS,
                sourceEventText = matcher.group(),
                sourceId = sourceId,
                sourceTitle = fromHtml(matcher.group(2))!!,
                user = User.required(
                    id = matcher.group(3).toInt(),
                    nick = userNick
                ),
                timeStamp = matcher.group(5).toInt().toLong(),
                msgCount = matcher.group(6).toInt(),
                messageId = 0,
                lastTimeStamp = 0,
                isImportant = false,
            )
        }
    }

    companion object {
        private val inspectorFavoritesPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        private val inspectorQmsPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        private val webSocketEventPattern: Pattern =
            Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]")
    }
}
