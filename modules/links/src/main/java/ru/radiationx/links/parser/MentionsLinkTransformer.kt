package ru.radiationx.links.parser

import ru.radiationx.coretypes.PageOffset
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter

//https://4pda.to/forum/index.php?act=mentions
//https://4pda.to/forum/index.php?act=mentions&st=0
class MentionsLinkTransformer {

    private companion object {
    }

    fun build(builder: LinkBuilderAdapter, link: Links.Board.Mentions): LinkUrlAdapter {
        with(builder) {
            query("act", "mentions")
            query("st", link.offset.value)
        }
        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.Board.Mentions? {
        if (url.query("act") != "mentions") return null
        val pageOffset = url.query("st")?.toIntOrNull()?.let { PageOffset(value = it) } ?: PageOffset.default
        return Links.Board.Mentions(offset = pageOffset)
    }

}