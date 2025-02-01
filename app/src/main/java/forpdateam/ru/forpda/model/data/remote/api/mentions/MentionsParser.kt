package forpdateam.ru.forpda.model.data.remote.api.mentions

import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionsData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class MentionsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Mentions

    fun parse(response: String): MentionsData {
        val items = patternProvider
            .getPattern(scope.scope, scope.main)
            .matcher(response)
            .map { matcher ->
                val state = if (matcher.group(1) == "read") {
                    MentionItem.STATE_READ
                } else {
                    MentionItem.STATE_UNREAD
                }
                val type = if (matcher.group(2).equals("Форум", ignoreCase = true)) {
                    MentionItem.TYPE_TOPIC
                } else {
                    MentionItem.TYPE_NEWS
                }
                MentionItem(
                    state = state,
                    type = type,
                    link = matcher.group(3),
                    title = matcher.group(4).fromHtml().orEmpty(),
                    desc = matcher.group(5).fromHtml().orEmpty(),
                    date = matcher.group(6),
                    nick = matcher.group(7).fromHtml().orEmpty()
                )
            }

        return MentionsData(
            items = items,
            pagination = Pagination.parseForum(response)
        )
    }
}
