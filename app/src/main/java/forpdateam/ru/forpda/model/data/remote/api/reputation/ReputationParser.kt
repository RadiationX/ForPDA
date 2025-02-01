package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class ReputationParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Reputation

    fun parse(response: String): RepData {
        val items = patternProvider
            .getPattern(scope.scope, scope.main)
            .matcher(response)
            .map { matcher ->
                RepItem(
                    userId = matcher.group(1).toInt(),
                    userNick = matcher.group(2).fromHtml()!!,
                    title = matcher.group(5).fromHtml()!!,
                    sourceUrl = matcher.group(3),
                    sourceTitle = matcher.group(4)?.fromHtml(),
                    image = matcher.group(6),
                    date = matcher.group(7)
                )
            }
        val pagination = Pagination.parseForum(response)

        return patternProvider
            .getPattern(scope.scope, scope.info)
            .matcher(response)
            .requireOnce { matcher ->
                RepData(
                    id = matcher.group(1).toInt(),
                    nick = matcher.group(2).fromHtml(),
                    positive = matcher.group(3)?.toInt() ?: 0,
                    negative = matcher.group(4)?.toInt() ?: 0,
                    items = items,
                    pagination = pagination
                )
            }
    }
}