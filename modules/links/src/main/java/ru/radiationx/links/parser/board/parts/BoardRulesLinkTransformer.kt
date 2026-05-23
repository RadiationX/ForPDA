package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter


class BoardRulesLinkTransformer {

    private companion object {
    }

    fun build(builder: LinkBuilderAdapter, link: Links.Board.Rules): LinkUrlAdapter {
        with(builder) {
            query("act", "boardrules")
        }
        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.Board.Rules? {
        if (url.query("act") != "boardrules") return null
        return Links.Board.Rules
    }

}