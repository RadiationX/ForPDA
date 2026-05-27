package forpdateam.ru.forpda.model.data.remote.api.search

import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.others.user.ForumPostUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.common.PaginationParser
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

class SearchParser @Inject constructor(
    private val patternProvider: PatternProvider,
    private val paginationParser: PaginationParser
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
            paginationParser.parseNews(response)
        } else {
            paginationParser.parseForum(response)
        }
        return SearchResult(
            items = items,
            settings = settings,
            pagination = pagination,
            html = null
        )
    }

    private fun parseNews(response: String) = patternProvider
        .getRegexParser(scope.scope, scope.articles)
        .map(response) { matcher ->
            SearchItem.News(
                id = ArticleId(matcher.require(1).toInt()),
                imageUrl = matcher.require(2),
                date = matcher.require(3),
                user = User(
                    id = UserId(matcher.require(4).toInt()),
                    nick = matcher.require(5).fromHtml()
                ),
                title = matcher.require(6).fromHtml(),
                body = matcher.require(7)
            )
        }

    private fun parseTopics(response: String) = patternProvider
        .getRegexParser(scope.scope, scope.forum_topics)
        .map(response) { matcher ->
            SearchItem.Topic(
                topicId = TopicId(matcher.require(1).toInt()),
                title = matcher.require(4).fromHtml(),
                desc = matcher.require(5).fromHtml(),
                forumId = ForumId(matcher.require(6).toInt()),
                user = User(
                    id = UserId(matcher.require(10).toInt()),
                    nick = matcher.require(11).fromHtml(),
                ),
                date = matcher.require(12)
            )
        }


    private fun parseForumPosts(response: String) = patternProvider
        .getRegexParser(scope.scope, scope.forum_posts)
        .map(response) { matcher ->
            val title = matcher.require(4).fromHtml()
            val post = ForumPost(
                topicId = TopicId(matcher.require(2).toInt()),
                id = PostId(matcher.require(3).toInt()),

                date = matcher.require(5),
                isOnline = matcher.require(7).contains("green"),
                user = ForumPostUser(
                    id = UserId(matcher.require(10).toInt()),
                    nick = matcher.require(9).fromHtml(),
                    avatar = matcher.require(8).let {
                        if (it.isNotEmpty()) "https://s.4pda.to/forum/uploads/$it" else null
                    },
                ),
                isCurator = matcher.get(11) != null,
                groupColor = matcher.require(12),
                group = matcher.require(13),
                canMinusRep = matcher.require(14).isNotEmpty(),
                reputation = matcher.require(15),
                canPlusRep = matcher.require(16).isNotEmpty(),
                canReport = matcher.require(17).isNotEmpty(),
                canEdit = matcher.require(18).isNotEmpty(),
                canDelete = matcher.require(19).isNotEmpty(),
                canQuote = matcher.require(20).isNotEmpty(),
                body = matcher.require(21),
            )
            SearchItem.Post(
                title = title,
                post = post
            )
        }
}