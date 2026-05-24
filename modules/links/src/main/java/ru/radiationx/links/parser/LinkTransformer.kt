package ru.radiationx.links.parser

import ru.radiationx.links.Link
import ru.radiationx.links.parser.board.BoardLinkTransformer
import ru.radiationx.links.parser.devdb.DevDbLinkTransformer
import ru.radiationx.links.parser.other.OtherLinkTransformer
import ru.radiationx.links.parser.site.SiteLinkTransformer
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlAdapter

class LinkTransformer(private val adapter: LinkUrlAdapter) {

    fun build(link: Link): LinkUrl {
        val builder = adapter.builder()
        builder.scheme("https")
        builder.host("4pda.to")
        return when (link) {
            is Link.Site -> SiteLinkTransformer.build(builder, link)
            is Link.DevDb -> DevDbLinkTransformer.build(builder, link)
            is Link.Board -> BoardLinkTransformer.build(builder, link)
            is Link.Other.ExternalLink -> OtherLinkTransformer.build(builder, link)
        }
    }

    fun parse(url: String): Link? {
        val linkUrl = adapter.parse(url) ?: return null
        return parse(linkUrl)
    }

    fun parse(url: LinkUrl): Link? {
        if (url.host != "4pda.to" && url.host != "4pda.ru") return null
        BoardLinkTransformer.parse(url)?.also { return it }
        DevDbLinkTransformer.parse(url)?.also { return it }
        SiteLinkTransformer.parse(url)?.also { return it }
        OtherLinkTransformer.parse(url)?.also { return it }
        return null
    }
}