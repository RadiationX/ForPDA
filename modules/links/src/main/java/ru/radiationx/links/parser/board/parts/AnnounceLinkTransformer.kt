package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.AnnounceId
import ru.radiationx.links.Links
import ru.radiationx.links.parser.helpers.ForumIdCase
import ru.radiationx.links.parser.helpers.parseForumId
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

//https://4pda.to/forum/index.php?act=announce&f=283&st=239
internal object AnnounceLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Announce): LinkUrl {
        with(builder) {
            query("act", "announce")
            query(link.announceId)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Announce? {
        if (url.query("act") != "announce") return null
        val announceId = url.parseAnnounceId() ?: return null
        return Links.Board.Announce(announceId = announceId)
    }


    private fun LinkUrl.parseAnnounceId(): AnnounceId? {
        val forumId = parseForumId(ForumIdCase.Forum) ?: return null
        val st = query("st")?.toIntOrNull() ?: return null
        return AnnounceId(forumId = forumId, st = st)
    }

    private fun LinkUrlBuilder.query(announceId: AnnounceId) {
        query(announceId.forumId, ForumIdCase.Forum)
        query("st", announceId.st)
    }

}