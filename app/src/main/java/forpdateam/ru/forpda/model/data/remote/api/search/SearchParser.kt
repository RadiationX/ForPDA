package forpdateam.ru.forpda.model.data.remote.api.search

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class SearchParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Search

    fun parse(response: String, settings: SearchSettings): SearchResult = SearchResult().also { result ->
        val isNews = settings.resourceType == SearchSettings.RESOURCE_NEWS.first
        val resultTopics = settings.result == SearchSettings.RESULT_TOPICS.first
        if (isNews) {
            patternProvider
                    .getPattern(scope.scope, scope.articles)
                    .matcher(response)
                    .findAll { matcher ->
                        result.items.add(SearchItem().apply {
                            id = matcher.group(1).toInt()
                            imageUrl = matcher.group(2)
                            date = matcher.group(3)
                            userId = matcher.group(4).toInt()
                            nick = matcher.group(5).fromHtml()
                            title = matcher.group(6).fromHtml()
                            body = matcher.group(7)
                        })
                    }
        } else {
            if (resultTopics) {
                patternProvider
                        .getPattern(scope.scope, scope.forum_topics)
                        .matcher(response)
                        .findAll { matcher ->
                            result.items.add(SearchItem().apply {
                                topicId = matcher.group(1).toInt()
                                //setId(matcher.group(1).toInt());
                                title = matcher.group(4).fromHtml()
                                desc = matcher.group(5).fromHtml()
                                forumId = matcher.group(6).toInt()
                                userId = matcher.group(10).toInt()
                                nick = matcher.group(11).fromHtml()
                                date = matcher.group(12)
                            })
                        }
            } else {
                patternProvider
                        .getPattern(scope.scope, scope.forum_posts)
                        .matcher(response)
                        .findAll { matcher ->
                            result.items.add(SearchItem().apply {
                                topicId = matcher.group(2).toInt()
                                id = matcher.group(3).toInt()
                                title = matcher.group(4).fromHtml()
                                date = matcher.group(5)
                                //setNumber(matcher.group(6).toInt());
                                isOnline = matcher.group(7).contains("green")
                                matcher.group(8)?.also {
                                    if (!it.isEmpty()) {
                                        avatar = "https://s.4pda.to/forum/uploads/$it"
                                    }
                                }
                                nick = matcher.group(9).fromHtml()
                                userId = matcher.group(10).toInt()
                                isCurator = matcher.group(11) != null
                                groupColor = matcher.group(12)
                                group = matcher.group(13)
                                canMinusRep = !matcher.group(14).isEmpty()
                                reputation = matcher.group(15)
                                canPlusRep = !matcher.group(16).isEmpty()
                                canReport = !matcher.group(17).isEmpty()
                                canEdit = !matcher.group(18).isEmpty()
                                canDelete = !matcher.group(19).isEmpty()
                                canQuote = !matcher.group(20).isEmpty()
                                body = matcher.group(21)
                            })
                        }
            }
        }

        if (isNews) {
            result.pagination = Pagination.parseNews(response)
        } else {
            result.pagination = Pagination.parseForum(response)
        }
        result.settings = settings
        return result
    }
}