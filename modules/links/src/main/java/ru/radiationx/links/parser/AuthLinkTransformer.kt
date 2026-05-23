package ru.radiationx.links.parser

import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter


class AuthLinkTransformer {

    private companion object {
    }

    fun build(builder: LinkBuilderAdapter, link: Links.Site): LinkUrlAdapter {

        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.Board.Auth? {


        return null
    }

}