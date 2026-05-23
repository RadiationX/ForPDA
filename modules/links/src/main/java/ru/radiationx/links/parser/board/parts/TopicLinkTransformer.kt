package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.PostId
import ru.radiationx.links.Links
import ru.radiationx.links.parser.helpers.TopicIdCase
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.parser.helpers.parseTopicId
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

//https://4pda.to/forum/index.php?act=findpost&pid=115499851&anchor=Spoil-115499851-1 - копирование спойлера
//https://4pda.to/forum/index.php?act=findpost&pid=115499851&anchor=entry115493099 - копирование спойлера
//https://4pda.to/forum/index.php?act=findpost&pid=115493099 - упоминание например
//https://4pda.to/forum/index.php?showtopic=1045802&anchor=entry115493099#Spoil-115499851-1
//https://4pda.to/forum/index.php?showtopic=1045802#entry115493099 - после findpost&pid=
//https://4pda.to/forum/index.php?showtopic=1045802#Spoil-115499851-1 - после findpost&pid=
//https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=114328645 - копирование ссылки на пост
//https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=115420723&anchor=Spoil-115420723-1 - копирование ссылки на пост
//https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=115420723#Spoil-115420723-1 - копирование ссылки на пост
//https://4pda.to/forum/index.php?showtopic=1045802&view=findpost&p=115420723&anchor=entry115420723 - копирование ссылки на пост
//https://4pda.to/forum/index.php?showtopic=1045802&mode=show&st=0 - показать результаты опроса
//https://4pda.to/forum/index.php?showtopic=208182&view=getnewpost
//https://4pda.to/forum/index.php?showtopic=208182&view=getlastpost#Spoil-115499851-1
internal object TopicLinkTransformer {

    private val entryRegex = Regex("entry(\\d+)")

    private val nodeRegex = Regex("(\\w+)-(\\d+)-(\\d+)")

    fun build(builder: LinkUrlBuilder, link: Links.Board.Topic): LinkUrl {
        with(builder) {
            when (link) {
                is Links.Board.Topic.FindPost -> {
                    query("act", "findpost")
                    query(link.postId, PostIdCase.FindPost)
                    query(link.anchor)
                }

                is Links.Board.Topic.ShowTopic -> {
                    query(link.topicId, TopicIdCase.ShowTopic)

                    when (link) {
                        is Links.Board.Topic.ShowTopic.Page -> {
                            query(link.offset)
                            if (link.showPollResults) {
                                query("mode", "show")
                            }
                            fragment(link.anchor)
                        }

                        is Links.Board.Topic.ShowTopic.FindPost -> {
                            query("view", "findpost")
                            query(link.postId, PostIdCase.FindPostInTopic)
                            query(link.anchor)
                        }

                        is Links.Board.Topic.ShowTopic.GetLastPost -> {
                            query("view", "getlastpost")
                        }

                        is Links.Board.Topic.ShowTopic.GetNewPost -> {
                            query("view", "getnewpost")
                        }
                    }
                }
            }
        }
        return builder.build()
    }

    private fun LinkUrlBuilder.query(anchor: Links.Board.Topic.Anchor?) {
        if (anchor == null) return
        query("anchor", anchor.value)
    }

    private fun LinkUrlBuilder.fragment(anchor: Links.Board.Topic.Anchor?) {
        if (anchor == null) return
        fragment(anchor.value)
    }

    fun parse(url: LinkUrl): Links.Board.Topic? {
        if (url.query("act") == "findpost") {
            val postId = url.parsePostId(PostIdCase.FindPost) ?: return null
            val anchor = url.parseAnchor()
            return Links.Board.Topic.FindPost(postId = postId, anchor = anchor)
        }
        return parseView(url)
    }


    private fun parseView(url: LinkUrl): Links.Board.Topic.ShowTopic? {
        val topicId = url.parseTopicId(TopicIdCase.ShowTopic) ?: return null
        val showPollResults = url.query("mode") == "show"
        val queryView = url.query("view")
        val offset = url.parsePageOffset()
        val anchor = url.parseAnchor()

        val topicByView = when (queryView) {
            "getnewpost" -> {
                Links.Board.Topic.ShowTopic.GetNewPost(
                    topicId = topicId,
                )
            }

            "getlastpost" -> {
                Links.Board.Topic.ShowTopic.GetLastPost(
                    topicId = topicId,
                )
            }

            "findpost" -> {
                url.parsePostId(PostIdCase.FindPostInTopic)?.let { postId ->
                    Links.Board.Topic.ShowTopic.FindPost(
                        topicId = topicId,
                        postId = postId,
                        anchor = anchor
                    )
                }
            }

            else -> {
                null
            }
        }
        if (topicByView != null) {
            return topicByView
        }
        return Links.Board.Topic.ShowTopic.Page(
            topicId = topicId,
            showPollResults = showPollResults,
            offset = offset,
            anchor = anchor
        )
    }

    private fun LinkUrl.parseAnchor(): Links.Board.Topic.Anchor? {
        val fragmentAnchor = fragment?.parseAnchor()
        if (fragmentAnchor != null) return fragmentAnchor
        return query("anchor")?.parseAnchor()
    }

    private fun String.parseAnchor(): Links.Board.Topic.Anchor? {
        entryRegex.find(this)?.also {
            val postId = it.groupValues[1].parsePostId() ?: return@also
            return Links.Board.Topic.Anchor.Post(postId = postId, value = this)
        }
        nodeRegex.find(this)?.also {
            val name = it.groupValues[1]
            val postId = it.groupValues[2].parsePostId() ?: return@also
            val number = it.groupValues[3].toIntOrNull() ?: return@also
            return Links.Board.Topic.Anchor.Node(name = name, postId = postId, number = number, value = this)
        }
        return null
    }

    private fun String.parsePostId(): PostId? {
        return toIntOrNull()?.let { PostId(it) }
    }

    private fun LinkUrl.parsePostId(case: PostIdCase): PostId? {
        return query(case.value)?.parsePostId()
    }

    private fun LinkUrlBuilder.query(postId: PostId, case: PostIdCase) {
        query(case.value, postId.id)
    }

    private enum class PostIdCase(val value: String) {
        FindPost("pid"),
        FindPostInTopic("p"),
    }
}