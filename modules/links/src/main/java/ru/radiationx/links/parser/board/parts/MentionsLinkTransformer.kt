package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.PageOffset
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl

//https://4pda.to/forum/index.php?act=mentions
//https://4pda.to/forum/index.php?act=mentions&st=0
internal object  MentionsLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Mentions): LinkUrl {
        with(builder) {
            query("act", "mentions")
            query("st", link.offset.value)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Mentions? {
        if (url.query("act") != "mentions") return null
        val pageOffset = url.query("st")?.toIntOrNull()?.let { PageOffset(value = it) } ?: PageOffset.default
        return Links.Board.Mentions(offset = pageOffset)
    }

}