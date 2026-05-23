package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl

//https://4pda.to/forum/index.php?act=announce&f=283&st=239
internal object AnnounceLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Announce): LinkUrl {
        with(builder) {
            query("act", "announce")
            query("f", link.announceId.forumId.id)
            query("st", link.announceId.st)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Announce? {
        if (url.query("act") != "announce") return null
        val forumId = url.query("f")?.toIntOrNull() ?: return null
        val st = url.query("st")?.toIntOrNull() ?: return null
        return Links.Board.Announce(announceId = AnnounceId(forumId = ForumId(forumId), st))
    }

}