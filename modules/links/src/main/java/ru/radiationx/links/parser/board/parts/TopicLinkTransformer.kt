package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter

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
class TopicLinkTransformer {

    private companion object {
        private val entryRegex = Regex("entry(\\d+)")
        private val nodeRegex = Regex("(\\w+)-(\\d+)-(\\d+)")
    }

    fun build(builder: LinkBuilderAdapter, link: Links.Board.Topic): LinkUrlAdapter {
        with(builder) {
            when (link) {
                is Links.Board.Topic.FindPost -> {
                    query("act", "findpost")
                    query("pid", link.postId.id)
                    link.anchor?.value?.also { query("anchor", it) }
                }

                is Links.Board.Topic.ShowTopic -> {
                    query("showtopic", link.topicId.id)

                    when (link) {
                        is Links.Board.Topic.ShowTopic.Page -> {
                            query("st", link.offset.value)
                            if (link.showPollResults) {
                                query("mode", "show")
                            }
                            link.anchor?.value?.also { fragment(it) }
                        }

                        is Links.Board.Topic.ShowTopic.FindPost -> {
                            query("view", "findpost")
                            query("p", link.postId)
                            link.anchor?.value?.also { query("anchor", it) }
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

    fun parse(url: LinkUrlAdapter): Links.Board.Topic? {
        if (url.query("act") == "findpost") {
            val postId = url.query("pid")?.toIntOrNull()?.let { PostId(it) } ?: return null
            val anchor = parseAnchor(url)
            return Links.Board.Topic.FindPost(postId = postId, anchor = anchor)
        }
        return parseView(url)
    }


    private fun parseView(url: LinkUrlAdapter): Links.Board.Topic.ShowTopic? {
        val topicId = url.query("showtopic")?.toIntOrNull()?.let { TopicId(it) } ?: return null
        val showPollResults = url.query("mode") == "show"
        val queryView = url.query("view")
        val offset = url.query("st")?.toIntOrNull()?.let { PageOffset(it) } ?: PageOffset.default
        val anchor = parseAnchor(url)

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
                url.query("p")?.toIntOrNull()?.let { postId ->
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

    private fun parseAnchor(url: LinkUrlAdapter): Links.Board.Topic.Anchor? {
        val fragmentAnchor = url.fragment?.let { parseAnchor(it) }
        if (fragmentAnchor != null) return fragmentAnchor
        return url.query("anchor")?.let { parseAnchor(it) }
    }

    private fun parseAnchor(value: String): Links.Board.Topic.Anchor? {
        entryRegex.find(value)?.also {
            val postId = it.groupValues[1].toIntOrNull()?.let { PostId(it) } ?: return@also
            return Links.Board.Topic.Anchor.Post(postId = postId, value = value)
        }
        nodeRegex.find(value)?.also {
            val name = it.groupValues[1]
            val postId = it.groupValues[2].toIntOrNull()?.let { PostId(it) } ?: return@also
            val number = it.groupValues[3].toIntOrNull() ?: return@also
            return Links.Board.Topic.Anchor.Node(name = name, postId = postId, number = number, value = value)
        }
        return null
    }
}