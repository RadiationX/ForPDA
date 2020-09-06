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

    fun parse(response: String): MentionsData = MentionsData().also { data ->
        patternProvider
                .getPattern(scope.scope, scope.main)
                .matcher(response)
                .findAll { matcher ->
                    data.items.add(MentionItem().apply {
                        state = if (matcher.group(1) == "read") MentionItem.STATE_READ else MentionItem.STATE_UNREAD
                        type = if (matcher.group(2).equals("Форум", ignoreCase = true)) MentionItem.TYPE_TOPIC else MentionItem.TYPE_NEWS
                        link = matcher.group(3)
                        title = matcher.group(4).fromHtml()
                        desc = matcher.group(5).fromHtml()
                        date = matcher.group(6)
                        nick = matcher.group(7).fromHtml()
                    })
                }
        data.pagination = Pagination.parseForum(response)
    }
}
