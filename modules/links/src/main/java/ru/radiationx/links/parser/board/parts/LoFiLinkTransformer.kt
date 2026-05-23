package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.parser.helpers.parseForumId
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.parser.helpers.parseTopicId
import ru.radiationx.links.url.LinkUrl

//https://4pda.to/forum/lofiversion/index.php?f956.html
//https://4pda.to/forum/lofiversion/index.php?f956-150.html
//https://4pda.to/forum/lofiversion/index.php?f956
//https://4pda.to/forum/lofiversion/index.php?f956-150
//https://4pda.to/forum/lofiversion/index.php?t956-150
//https://4pda.to/forum/lofiversion/index.php?t956
internal object LoFiLinkTransformer {

    private val queryRegex = Regex("([tf])(\\d+)(?:-(\\d+))?")

    fun parse(url: LinkUrl): Links.Board? {
        if (url.segment(1) != "lofiversion") return null
        val match = queryRegex.find(url.fullQuery().orEmpty()) ?: return null
        val type = match.groupValues[1]
        val idStr = match.groupValues[2]
        val offset = match.groupValues.getOrNull(3).parsePageOffset()

        return when (type) {
            "t" -> Links.Board.Topic.ShowTopic.Page(
                topicId = idStr.parseTopicId() ?: return null,
                showPollResults = false,
                offset = offset,
                anchor = null
            )

            "f" -> Links.Board.Forum(
                forumId = idStr.parseForumId() ?: return null,
                offset = offset
            )

            else -> null
        }
    }

}