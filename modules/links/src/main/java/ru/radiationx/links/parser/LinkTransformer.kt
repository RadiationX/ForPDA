package ru.radiationx.links.parser

import ru.radiationx.links.Links
import ru.radiationx.links.parser.board.BoardLinkTransformer
import ru.radiationx.links.parser.devdb.DevDbLinkTransformer
import ru.radiationx.links.parser.site.SiteLinkTransformer
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlAdapter

class LinkTransformer(private val adapter: LinkUrlAdapter) {

    fun build(link: Links): LinkUrl {
        val builder = adapter.builder()
        builder.scheme("https")
        builder.host("4pda.to")
        return when (link) {
            is Links.Site -> SiteLinkTransformer.build(builder, link)
            is Links.DevDb -> DevDbLinkTransformer.build(builder, link)
            is Links.Board -> BoardLinkTransformer.build(builder, link)
        }
    }

    fun parse(url: String): Links? {
        return parse(adapter.parse(url))
    }

    fun parse(url: LinkUrl): Links? {
        BoardLinkTransformer.parse(url)?.also { return it }
        DevDbLinkTransformer.parse(url)?.also { return it }
        SiteLinkTransformer.parse(url)?.also { return it }
        return null
    }
}