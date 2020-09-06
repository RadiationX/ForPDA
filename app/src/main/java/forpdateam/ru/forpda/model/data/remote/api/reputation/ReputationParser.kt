package forpdateam.ru.forpda.model.data.remote.api.reputation

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class ReputationParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Reputation

    fun parse(response: String): RepData = RepData().also { data ->
        patternProvider
                .getPattern(scope.scope, scope.info)
                .matcher(response)
                .findOnce { matcher ->
                    data.id = matcher.group(1).toInt()
                    data.nick = matcher.group(2).fromHtml()
                    matcher.group(3)?.also {
                        data.positive = it.toInt()
                    }
                    matcher.group(4)?.also {
                        data.negative = it.toInt()
                    }
                }

        patternProvider
                .getPattern(scope.scope, scope.main)
                .matcher(response)
                .findAll { matcher ->
                    data.items.add(RepItem().apply {
                        userId = matcher.group(1).toInt()
                        userNick = matcher.group(2).fromHtml()
                        matcher.group(3)?.also {
                            sourceUrl = it
                            sourceTitle = matcher.group(4).fromHtml()
                        }
                        title = matcher.group(5).fromHtml()
                        image = matcher.group(6)
                        date = matcher.group(7)
                    })
                }
        data.pagination = Pagination.parseForum(response)
        return data
    }

}