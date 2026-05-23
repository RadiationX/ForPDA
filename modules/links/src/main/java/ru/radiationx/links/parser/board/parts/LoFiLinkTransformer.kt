package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrlAdapter

//https://4pda.to/forum/lofiversion/index.php?f956.html
//https://4pda.to/forum/lofiversion/index.php?f956-150.html
//https://4pda.to/forum/lofiversion/index.php?f956
//https://4pda.to/forum/lofiversion/index.php?f956-150
//https://4pda.to/forum/lofiversion/index.php?t956-150
//https://4pda.to/forum/lofiversion/index.php?t956
class LoFiLinkTransformer {

    private companion object {
        private val queryRegex = Regex("([tf])(\\d+)(?:-(\\d+))?")
    }

    fun parse(url: LinkUrlAdapter): Links.Board? {
        if (url.segment(1) != "lofiversion") return null
        val match = queryRegex.find(url.fullQuery().orEmpty()) ?: return null
        val type = match.groupValues[1]
        val id = match.groupValues[2].toIntOrNull() ?: return null
        val offset = match.groupValues.getOrNull(3)
            ?.toIntOrNull()
            ?.let { PageOffset(it) }
            ?: PageOffset.default

        return when (type) {
            "t" -> Links.Board.Topic.ShowTopic.Page(
                topicId = TopicId(id = id),
                showPollResults = false,
                offset = offset,
                anchor = null
            )

            "f" -> Links.Board.Forum(
                forumId = ForumId(id = id),
                offset = offset
            )

            else -> null
        }
    }

}