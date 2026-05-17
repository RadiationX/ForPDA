package forpdateam.ru.forpda.model.data.remote.api.inspector

import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.17.
 */
class InspectorParser @Inject constructor(): BaseParser() {

    fun parseFavoritesEvents(response: String): List<InspectorItem.Favorite> {
        return inspectorFavoritesPattern.matcher(response).map { matcher ->
            InspectorItem.Favorite(
                topicId = matcher.group(1).toInt(),
                sourceTitle = matcher.group(2).fromHtml()!!,
                msgCount = matcher.group(3).toInt(),
                user = User.required(
                    id = matcher.group(4).toInt(),
                    nick = matcher.group(5).fromHtml()
                ),
                timeStamp = matcher.group(6).toLong() * 1000L,
                lastTimeStamp = matcher.group(7).toLong() * 1000L,
                isImportant = matcher.group(8) == "1",
                rawContent = matcher.group()
            )
        }
    }

    fun parseQmsEvents(response: String): List<InspectorItem.Qms> {
        return inspectorQmsPattern.matcher(response).map { matcher ->
            val sourceId = matcher.group(1).toInt()
            var userNick = matcher.group(4).fromHtml()!!
            if (userNick.isEmpty() && sourceId == 0) {
                userNick = "Сообщения 4PDA"
            }
            InspectorItem.Qms(
                themeId = sourceId,
                sourceTitle = matcher.group(2).fromHtml()!!,
                user = User.required(
                    id = matcher.group(3).toInt(),
                    nick = userNick
                ),
                timeStamp = matcher.group(5).toLong() * 1000L,
                msgCount = matcher.group(6).toInt(),
                messageId = matcher.group(7).toInt(),
                rawContent = matcher.group()
            )
        }
    }

    companion object {
        private val inspectorFavoritesPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
        private val inspectorQmsPattern: Pattern =
            Pattern.compile("(\\d+) \"([\\s\\S]*?)\" (\\d+) \"([\\s\\S]*?)\" (\\d+) (\\d+) (\\d+)")
    }
}
