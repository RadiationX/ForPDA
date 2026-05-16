package forpdateam.ru.forpda.model.data.remote.api.mentions

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class MentionsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Mentions

    fun parse(response: String): MentionsData {
        val items = patternProvider
            .getRegexParser(scope.scope, scope.main)
            .map(response) { matcher ->
                val state = if (matcher.require(1) == "read") {
                    MentionItem.STATE_READ
                } else {
                    MentionItem.STATE_UNREAD
                }
                val type = if (matcher.require(2).equals("Форум", ignoreCase = true)) {
                    MentionItem.TYPE_TOPIC
                } else {
                    MentionItem.TYPE_NEWS
                }
                MentionItem(
                    state = state,
                    type = type,
                    link = matcher.require(3),
                    title = matcher.require(4).fromHtml(),
                    desc = matcher.require(5).fromHtml(),
                    date = matcher.require(6),
                    nick = matcher.require(7).fromHtml()
                )
            }

        return MentionsData(
            items = items,
            pagination = Pagination.parseForum(response)
        )
    }
}
