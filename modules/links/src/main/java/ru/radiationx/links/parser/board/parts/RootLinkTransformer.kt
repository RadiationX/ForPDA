package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/forum/
//https://4pda.to/forum/index.php
//https://4pda.to/forum/index.php?act=idx
internal object RootLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Root): LinkUrl {
        with(builder) {
            query("act", "idx")
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Root? {
        val segment1 = url.segment(1)
        val segment2 = url.segment(2)
        if (!(segment1.isNullOrEmpty() || segment1 == "index.php") || !segment2.isNullOrEmpty()) {
            return null
        }
        val act = url.query("act")
        if (!act.isNullOrEmpty() && act != "idx") {
            return null
        }
        return Links.Board.Root
    }

}