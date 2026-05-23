package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder


internal object AuthLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Auth): LinkUrl {
        with(builder) {
            when (link) {
                Link.Board.Auth.LoginForm -> {
                    query("act", "auth")
                }
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Auth? {
        if (url.query("act") != "auth") return null
        return Link.Board.Auth.LoginForm
    }

}