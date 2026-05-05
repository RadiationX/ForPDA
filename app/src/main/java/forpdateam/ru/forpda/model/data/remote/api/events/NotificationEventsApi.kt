package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.fromHtml
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.07.17.
 */
class NotificationEventsApi(private val webClient: IWebClient) {
    fun parseWebSocketEvent(message: String): NotificationEvent? {
        val matcher = webSocketEventPattern.matcher(message)
        return parseWebSocketEvent(matcher)
    }

    fun parseWebSocketEvent(matcher: Matcher): NotificationEvent? {

        if (!matcher.find()) return null
        //// TODO: 02.10.17 сделать обратку нотификации форума
        val source = when (matcher.group(3)) {
            NotificationEvent.SRC_TYPE_THEME -> NotificationEvent.Source.THEME
            NotificationEvent.SRC_TYPE_SITE -> NotificationEvent.Source.SITE
            NotificationEvent.SRC_TYPE_QMS -> NotificationEvent.Source.QMS
            else -> return null
        }
        val type = when (matcher.group(5).toInt()) {
            NotificationEvent.SRC_EVENT_NEW -> NotificationEvent.Type.NEW
            NotificationEvent.SRC_EVENT_READ -> NotificationEvent.Type.READ
            NotificationEvent.SRC_EVENT_MENTION -> NotificationEvent.Type.MENTION
            NotificationEvent.SRC_EVENT_HAT_EDITED -> NotificationEvent.Type.HAT_EDITED
            else -> return null
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

    suspend fun getFavoritesEvents(): List<NotificationEvent> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=fav")
        return getFavoritesEvents(response.body)
    }

    fun getFavoritesEvents(response: String): List<NotificationEvent> {
        val events: MutableList<NotificationEvent> = ArrayList()
        val matcher = inspectorFavoritesPattern.matcher(response)
        while (matcher.find()) {
            //Log.e("events_lalala", "Matcher add event: " + matcher.group());
            val event = getFavoritesEvent(matcher)
            events.add(event)
        }
        return events
    }

    fun getFavoritesEvent(matcher: Matcher): NotificationEvent {
        val event = NotificationEvent(
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
        return event
    }

    // also can use CODE=mentions i guess

    suspend fun getQmsEvents(): List<NotificationEvent> {
        val response = webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=qms")
        return getQmsEvents(response.body)
    }

    fun getQmsEvents(response: String): List<NotificationEvent> {
        val events: MutableList<NotificationEvent> = ArrayList()
        val matcher = inspectorQmsPattern.matcher(response)
        while (matcher.find()) {
            val event = getQmsEvent(matcher)
            events.add(event)
        }
        return events
    }

    fun getQmsEvent(matcher: Matcher): NotificationEvent {
        val sourceId = matcher.group(1).toInt()
        var userNick = fromHtml(matcher.group(4))!!
        if (userNick.isEmpty() && sourceId == 0) {
            userNick = "Сообщения 4PDA"
        }
        val event = NotificationEvent(
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
        return event
    }

    companion object {
        val inspectorFavoritesPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        val inspectorQmsPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        val webSocketEventPattern: Pattern =
            Pattern.compile("\\[(\\d+),(\\d+),\"([\\s\\S])(\\d+)\",(\\d+),(\\d+)\\]")
    }
}
