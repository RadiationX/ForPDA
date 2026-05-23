package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl

//https://4pda.to/forum/index.php?forums=285&topics=1026049&act=search&source=pst&query=kino
//https://4pda.to/forum/index.php?act=search&query=kino&username=&forums%5B%5D=285&topics=1026049&source=pst&sort=rel&result=posts
internal object  SearchLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Search): LinkUrl {
        with(builder) {
            query("act", "search")
            query("query", link.query)
            query("username", link.nick)
            link.forums.forEach {
                when (it) {
                    Links.Board.Search.Forum.All -> query("forums[]", "all")
                    is Links.Board.Search.Forum.Id -> query("forums[]", it.forumId.id)
                }
            }
            query("subforums", link.subforums)
            link.topics.forEach {
                query("topics[]", it.id)
            }
            val source = when (link.source) {
                Links.Board.Search.Source.All -> "all"
                Links.Board.Search.Source.Title -> "top"
                Links.Board.Search.Source.Post -> "pst"
            }
            query("source", source)

            val sort = when (link.sort) {
                Links.Board.Search.Sort.Relevancy -> "rel"
                Links.Board.Search.Sort.DateAsc -> "da"
                Links.Board.Search.Sort.DateDesc -> "dd"
            }
            query("sort", sort)

            val result = when (link.result) {
                Links.Board.Search.Result.Topics -> "topics"
                Links.Board.Search.Result.Posts -> "posts"
            }
            query("result", result)

            query("st", link.offset.value)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Search? {
        if (url.query("act") != "search") return null

        val query = url.query("query").orEmpty()
        val nick = url.query("username").orEmpty()
        val forums = parseMultipleQuery(url, "forums").mapNotNull { value ->
            if (value == "all") {
                Links.Board.Search.Forum.All
            } else {
                val forumId = value.toIntOrNull()?.let { ForumId(it) } ?: return@mapNotNull null
                Links.Board.Search.Forum.Id(forumId = forumId)
            }
        }.toSet()
        val subforums = url.query("subforums") == "1"
        val topics = parseMultipleQuery(url, "topics").mapNotNull { value ->
            value.toIntOrNull()?.let { TopicId(it) }
        }.toSet()
        val source = when (url.query("source")) {
            "pst" -> Links.Board.Search.Source.Post
            "top" -> Links.Board.Search.Source.Title
            "all" -> Links.Board.Search.Source.All
            else -> Links.Board.Search.Source.All
        }
        val sort = when (url.query("sort")) {
            "rel" -> Links.Board.Search.Sort.Relevancy
            "da" -> Links.Board.Search.Sort.DateAsc
            "dd" -> Links.Board.Search.Sort.DateDesc
            else -> Links.Board.Search.Sort.Relevancy
        }
        val result = when (url.query("result")) {
            "posts" -> Links.Board.Search.Result.Posts
            "topics" -> Links.Board.Search.Result.Topics
            else -> Links.Board.Search.Result.Posts
        }
        val offset = url.query("st")?.toIntOrNull()?.let { PageOffset(it) } ?: PageOffset.default
        return Links.Board.Search(
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

    private fun parseMultipleQuery(url: LinkUrl, name: String): List<String> {
        return buildList {
            addAll(url.queries(name))
            addAll(url.queries("${name}[]"))
        }
    }

}