package ru.radiationx.links.parser

import okhttp3.HttpUrl.Companion.toHttpUrl
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter


class BoardLinkTransformer {

    private companion object {
    }

    fun build(builder: LinkBuilderAdapter, link: Links.Board.Auth): LinkUrlAdapter {
        with(builder) {
            when (link) {
                Links.Board.Auth.LoginForm -> {
                    query("act", "auth")
                }
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.Board.Auth? {
        if (url.segment(0) != "forum") return null
        if (url.query("act") != "auth") return null
        url.fullQuery()
        "".toHttpUrl().que
        return Links.Board.Auth.LoginForm
    }

}