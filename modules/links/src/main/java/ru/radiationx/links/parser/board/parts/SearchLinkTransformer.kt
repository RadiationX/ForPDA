package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
import ru.radiationx.links.parser.helpers.ForumIdCase
import ru.radiationx.links.parser.helpers.TopicIdCase
import ru.radiationx.links.parser.helpers.parseForumId
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.parser.helpers.parseTopicId
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

//https://4pda.to/forum/index.php?forums=285&topics=1026049&act=search&source=pst&query=kino
//https://4pda.to/forum/index.php?act=search&query=kino&username=&forums%5B%5D=285&topics=1026049&source=pst&sort=rel&result=posts
internal object SearchLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Search): LinkUrl {
        with(builder) {
            query("act", "search")
            query("query", link.query)
            query("username", link.nick)
            link.forums.forEach {
                when (it) {
                    Link.Board.Search.Forum.All -> query(ForumIdCase.SearchArray.value, "all")
                    is Link.Board.Search.Forum.Id -> query(it.forumId, ForumIdCase.SearchArray)
                }
            }
            query("subforums", link.subforums)
            link.topics.forEach {
                query(it, TopicIdCase.SearchArray)
            }
            val source = when (link.source) {
                Link.Board.Search.Source.All -> "all"
                Link.Board.Search.Source.Title -> "top"
                Link.Board.Search.Source.Post -> "pst"
            }
            query("source", source)

            val sort = when (link.sort) {
                Link.Board.Search.Sort.Relevancy -> "rel"
                Link.Board.Search.Sort.DateAsc -> "da"
                Link.Board.Search.Sort.DateDesc -> "dd"
            }
            query("sort", sort)

            val result = when (link.result) {
                Link.Board.Search.Result.Topics -> "topics"
                Link.Board.Search.Result.Posts -> "posts"
            }
            query("result", result)

            query(link.offset)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Search? {
        if (url.query("act") != "search") return null

        val query = url.query("query").orEmpty()
        val nick = url.query("username").orEmpty()
        val forums = parseMultipleQuery(url, ForumIdCase.Search.value, ForumIdCase.SearchArray.value).mapNotNull { value ->
            if (value == "all") {
                Link.Board.Search.Forum.All
            } else {
                val forumId = value.parseForumId() ?: return@mapNotNull null
                Link.Board.Search.Forum.Id(forumId = forumId)
            }
        }.toSet()
        val subforums = url.query("subforums") == "1"
        val topics = parseMultipleQuery(url, TopicIdCase.Search.value, TopicIdCase.SearchArray.value).mapNotNull { value ->
            value.parseTopicId()
        }.toSet()
        val source = when (url.query("source")) {
            "pst" -> Link.Board.Search.Source.Post
            "top" -> Link.Board.Search.Source.Title
            "all" -> Link.Board.Search.Source.All
            else -> Link.Board.Search.Source.All
        }
        val sort = when (url.query("sort")) {
            "rel" -> Link.Board.Search.Sort.Relevancy
            "da" -> Link.Board.Search.Sort.DateAsc
            "dd" -> Link.Board.Search.Sort.DateDesc
            else -> Link.Board.Search.Sort.Relevancy
        }
        val result = when (url.query("result")) {
            "posts" -> Link.Board.Search.Result.Posts
            "topics" -> Link.Board.Search.Result.Topics
            else -> Link.Board.Search.Result.Posts
        }
        val offset = url.parsePageOffset()
        return Link.Board.Search(
            query = query,
            nick = nick,
            forums = forums,
            subforums = subforums,
            topics = topics,
            source = source,
            sort = sort,
            result = result,
            offset = offset
        )
    }

    private fun parseMultipleQuery(url: LinkUrl, vararg name: String): List<String> {
        return buildList {
            name.forEach {
                addAll(url.queries(it))
            }
        }
    }

}