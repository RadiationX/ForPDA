package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.common.PaginationParser
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class ReputationParser @Inject constructor(
    private val patternProvider: PatternProvider,
    private val paginationParser: PaginationParser
) : BaseParser() {

    private val scope = ParserPatterns.Reputation

    fun parse(response: String): RepData {
        val items = patternProvider
            .getRegexParser(scope.scope, scope.main)
            .map(response) { matcher ->
                RepItem(
                    user = User(
                        id = UserId(matcher.require(1).toInt()),
                        nick = matcher.require(2).fromHtml()
                    ),
                    title = matcher.require(5).fromHtml(),
                    sourceUrl = matcher.get(3),
                    sourceTitle = matcher.get(4)?.fromHtml(),
                    image = matcher.require(6),
                    date = matcher.require(7)
                )
            }
        val pagination = paginationParser.parseForum(response)

        return patternProvider
            .getRegexParser(scope.scope, scope.info)
            .requireOnce(response) { matcher ->
                RepData(
                    id = UserId(matcher.require(1).toInt()),
                    nick = matcher.require(2).fromHtml(),
                    positive = matcher.get(3)?.toInt() ?: 0,
                    negative = matcher.get(4)?.toInt() ?: 0,
                    items = items,
                    pagination = pagination
                )
            }
    }
}