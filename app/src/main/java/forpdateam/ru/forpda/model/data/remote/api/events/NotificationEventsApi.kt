package forpdateam.ru.forpda.model.data.remote.api.events

import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
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
        var wsEvent: NotificationEvent? = null

        if (matcher.find()) {
            wsEvent = NotificationEvent(
                NotificationEvent.Type.NEW,
                NotificationEvent.Source.THEME
            )

            //wsEvent.setUnknown1(Integer.parseInt(matcher.group(1)));
            //wsEvent.setUnknown2(Integer.parseInt(matcher.group(2)));
            when (matcher.group(3)) {
                NotificationEvent.SRC_TYPE_THEME -> wsEvent.source = NotificationEvent.Source.THEME
                NotificationEvent.SRC_TYPE_SITE -> wsEvent.source = NotificationEvent.Source.SITE
                NotificationEvent.SRC_TYPE_QMS -> wsEvent.source = NotificationEvent.Source.QMS
                else ->                     //// TODO: 02.10.17 сделать обратку нотификации форума
                    return null
            }

            wsEvent.sourceId = matcher.group(4).toInt()

            when (matcher.group(5).toInt()) {
                NotificationEvent.SRC_EVENT_NEW -> wsEvent.type = NotificationEvent.Type.NEW
                NotificationEvent.SRC_EVENT_READ -> wsEvent.type = NotificationEvent.Type.READ
                NotificationEvent.SRC_EVENT_MENTION -> wsEvent.type = NotificationEvent.Type.MENTION
                NotificationEvent.SRC_EVENT_HAT_EDITED -> wsEvent.type =
                    NotificationEvent.Type.HAT_EDITED
            }
            wsEvent.messageId = matcher.group(6).toInt()
        }

        return wsEvent
    }

    @get:Throws(Exception::class)
    val favoritesEvents: List<NotificationEvent>
        get() {
            val response =
                webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=fav")
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
            NotificationEvent.Type.NEW,
            NotificationEvent.Source.THEME
        )
        event.sourceEventText = matcher.group()
        event.source = NotificationEvent.Source.THEME
        event.type = NotificationEvent.Type.NEW
        event.sourceId = matcher.group(1).toInt()
        event.sourceTitle = fromHtml(matcher.group(2))!!
        event.msgCount = matcher.group(3).toInt()
        event.userId = matcher.group(4).toInt()
        event.userNick = fromHtml(matcher.group(5))!!
        event.timeStamp = matcher.group(6).toInt().toLong()
        event.lastTimeStamp = matcher.group(7).toInt().toLong()
        event.isImportant = matcher.group(8) == "1"
        return event
    }

    @get:Throws(Exception::class)
    val qmsEvents: List<NotificationEvent>
        get() {
            val response =
                webClient.get("https://4pda.to/forum/index.php?act=inspector&CODE=qms")
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
        val event = NotificationEvent(
            NotificationEvent.Type.NEW,
            NotificationEvent.Source.QMS
        )
        event.sourceEventText = matcher.group()
        event.source = NotificationEvent.Source.QMS
        event.type = NotificationEvent.Type.NEW
        event.sourceId = matcher.group(1).toInt()
        event.sourceTitle = fromHtml(matcher.group(2))!!
        event.userId = matcher.group(3).toInt()
        event.userNick = fromHtml(matcher.group(4))!!
        event.timeStamp = matcher.group(5).toInt().toLong()
        event.msgCount = matcher.group(6).toInt()
        if (event.userNick.isEmpty() && event.sourceId == 0) {
            event.userNick = "Сообщения 4PDA"
        }
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
