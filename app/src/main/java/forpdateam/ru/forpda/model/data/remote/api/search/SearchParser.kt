package forpdateam.ru.forpda.model.data.remote.api.search

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class SearchParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Search

    fun parse(response: String, settings: SearchSettings): SearchResult {
        val isNews = settings.resourceType == SearchSettings.RESOURCE_NEWS.first
        val resultTopics = settings.result == SearchSettings.RESULT_TOPICS.first
        val items = if (isNews) {
            parseNews(response)
        } else {
            if (resultTopics) {
                parseTopics(response)
            } else {
                parseForumPosts(response)
            }
        }
        val pagination = if (isNews) {
            Pagination.parseNews(response)
        } else {
            Pagination.parseForum(response)
        }
        return SearchResult(
            items = items,
            settings = settings,
            pagination = pagination,
            html = null
        )
    }

    private fun parseNews(response: String) = patternProvider
        .getPattern(scope.scope, scope.articles)
        .matcher(response)
        .map { matcher ->
            SearchItem.News(
                id = matcher.group(1).toInt(),
                imageUrl = matcher.group(2),
                date = matcher.group(3),
                userId = matcher.group(4).toInt(),
                nick = matcher.group(5).fromHtml()!!,
                title = matcher.group(6).fromHtml()!!,
                body = matcher.group(7)
            )
        }

    private fun parseTopics(response: String) = patternProvider
        .getPattern(scope.scope, scope.forum_topics)
        .matcher(response)
        .map { matcher ->
            SearchItem.Topic(
                topicId = matcher.group(1).toInt(),
                title = matcher.group(4).fromHtml()!!,
                desc = matcher.group(5).fromHtml()!!,
                forumId = matcher.group(6).toInt(),
                userId = matcher.group(10).toInt(),
                nick = matcher.group(11).fromHtml()!!,
                date = matcher.group(12)
            )
        }

    private fun parseForumPosts(response: String) = patternProvider
        .getPattern(scope.scope, scope.forum_posts)
        .matcher(response)
        .map { matcher ->
            val title = matcher.group(4).fromHtml()!!
            val post = ForumPost(
                topicId = matcher.group(2).toInt(),
                id = matcher.group(3).toInt(),

                date = matcher.group(5),
                isOnline = matcher.group(7).contains("green"),
                avatar = matcher.group(8).let {
                    if (it.isNotEmpty()) "https://s.4pda.to/forum/uploads/$it" else null
                },
                nick = matcher.group(9).fromHtml()!!,
                userId = matcher.group(10).toInt(),
                isCurator = matcher.group(11) != null,
                groupColor = matcher.group(12),
                group = matcher.group(13),
                canMinusRep = matcher.group(14).isNotEmpty(),
                reputation = matcher.group(15),
                canPlusRep = matcher.group(16).isNotEmpty(),
                canReport = matcher.group(17).isNotEmpty(),
                canEdit = matcher.group(18).isNotEmpty(),
                canDelete = matcher.group(19).isNotEmpty(),
                canQuote = matcher.group(20).isNotEmpty(),
                body = matcher.group(21),
            )
            SearchItem.ForumPost(
                title = title,
                post = post
            )
        }
}