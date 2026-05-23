package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl

//https://4pda.to/forum/index.php?showuser=4575561
internal object  ProfileLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Profile): LinkUrl {
        with(builder) {
            query("showuser", link.userId.id)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Profile? {
        val userId = url.query("showuser")?.toIntOrNull()?.let { UserId(it) } ?: return null
        return Links.Board.Profile(userId = userId)
    }

}