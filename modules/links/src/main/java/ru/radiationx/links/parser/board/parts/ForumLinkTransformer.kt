package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
import ru.radiationx.links.parser.helpers.ForumIdCase
import ru.radiationx.links.parser.helpers.parseForumId
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/forum/index.php?showforum=956
//https://4pda.to/forum/index.php?showforum=956&st=150
internal object ForumLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Forum): LinkUrl {
        with(builder) {
            query(link.forumId, ForumIdCase.ShowForum)
            query(link.offset)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Forum? {
        val forumId = url.parseForumId(ForumIdCase.ShowForum) ?: return null
        val offset = url.parsePageOffset()
        return Link.Board.Forum(forumId = forumId, offset = offset)
    }

}