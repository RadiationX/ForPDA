package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder


internal object AuthLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Auth): LinkUrl {
        with(builder) {
            when (link) {
                Links.Board.Auth.LoginForm -> {
                    query("act", "auth")
                }
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Auth? {
        if (url.query("act") != "auth") return null
        return Links.Board.Auth.LoginForm
    }

}