package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/forum/index.php?act=mentions
//https://4pda.to/forum/index.php?act=mentions&st=0
internal object MentionsLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Mentions): LinkUrl {
        with(builder) {
            query("act", "mentions")
            query(link.offset)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Mentions? {
        if (url.query("act") != "mentions") return null
        val pageOffset = url.parsePageOffset()
        return Link.Board.Mentions(offset = pageOffset)
    }

}