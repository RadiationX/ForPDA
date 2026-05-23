package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl


internal object  RulesLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Rules): LinkUrl {
        with(builder) {
            query("act", "boardrules")
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Rules? {
        if (url.query("act") != "boardrules") return null
        return Link.Board.Rules
    }

}