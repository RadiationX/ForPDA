package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl


internal object  RulesLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Rules): LinkUrl {
        with(builder) {
            query("act", "boardrules")
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Rules? {
        if (url.query("act") != "boardrules") return null
        return Links.Board.Rules
    }

}