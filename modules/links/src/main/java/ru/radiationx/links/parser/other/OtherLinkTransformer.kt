package ru.radiationx.links.parser.other

import ru.radiationx.links.Link
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/stat/go?u=https%3A%2F%2Fgadgets360.com%2Fscience%2Fnews%2Ftransparent-perovskite-solar-cells-could-turn-windows-into-power-sources-11500336%23rss-gadgets-news
//https://4pda.to/pages/go?u=https%3A%2F%2Fgadgets360.com%2Fscience%2Fnews%2Ftransparent-perovskite-solar-cells-could-turn-windows-into-power-sources-11500336%23rss-gadgets-news
internal object OtherLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Other): LinkUrl {
        with(builder) {
            when (link) {
                is Link.Other.ExternalLink -> {
                    segment("stat")
                    segment("go")
                    query("u", link.url)
                    link.e?.also { query("e", it) }
                }
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Other? {
        parseExternal(url)?.also { return it }
        return null
    }

    private fun parseExternal(url: LinkUrl): Link.Other.ExternalLink? {
        if (url.segment(0)?.let { it != "stat" && it != "pages" } == true) return null
        if (url.segment(1) != "go") return null
        val queryUrl = url.query("u") ?: return null
        val queryE = url.query("e")
        return Link.Other.ExternalLink(queryUrl, queryE)
    }

}