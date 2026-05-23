package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/forum/index.php?showforum=956
//https://4pda.to/forum/index.php?showforum=956&st=150
internal object ForumLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Forum): LinkUrl {
        with(builder) {
            query("showforum", link.forumId.id)
            query("st", link.offset.value)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Forum? {
        val forumId = url.query("showforum")?.toIntOrNull()?.let { ForumId(it) } ?: return null
        val offset = url.query("st")?.toIntOrNull()?.let { PageOffset(it) } ?: PageOffset.default
        return Links.Board.Forum(forumId = forumId, offset = offset)
    }

}