package forpdateam.ru.forpda.model.data.remote.api.inspector

import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

/**
 * Created by radiationx on 31.07.17.
 */
class InspectorParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Inspector

    fun parseFavoritesEvents(response: String): List<InspectorItem.Favorite> {
        return patternProvider
            .getRegexParser(scope.scope, scope.favorites)
            .map(response) { matcher ->
                InspectorItem.Favorite(
                    topicId = TopicId(matcher.require(1).toInt()),
                    sourceTitle = matcher.require(2).fromHtml(),
                    msgCount = matcher.require(3).toInt(),
                    user = User(
                        id = UserId(matcher.require(4).toInt()),
                        nick = matcher.require(5).fromHtml()
                    ),
                    timeStamp = matcher.require(6).toLong() * 1000L,
                    lastTimeStamp = matcher.require(7).toLong() * 1000L,
                    isImportant = matcher.require(8) == "1",
                    rawContent = matcher.require(0)
                )
            }
    }

    fun parseQmsEvents(response: String): List<InspectorItem.Qms> {
        return patternProvider
            .getRegexParser(scope.scope, scope.qms)
            .map(response) { matcher ->
                val sourceId = matcher.require(1).toInt()
                var userNick = matcher.require(4).fromHtml()
                if (userNick.isEmpty() && sourceId == 0) {
                    userNick = "Сообщения 4PDA"
                }
                InspectorItem.Qms(
                    themeId = QmsThreadId(sourceId),
                    sourceTitle = matcher.require(2).fromHtml(),
                    user = User(
                        id = UserId(matcher.require(3).toInt()),
                        nick = userNick
                    ),
                    timeStamp = matcher.require(5).toLong() * 1000L,
                    msgCount = matcher.require(6).toInt(),
                    messageId = QmsMessageId(matcher.require(7).toInt()),
                    rawContent = matcher.require(0)
                )
            }
    }
}
