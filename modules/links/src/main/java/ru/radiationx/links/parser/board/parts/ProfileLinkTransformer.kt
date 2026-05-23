package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.parser.helpers.UserIdCase
import ru.radiationx.links.parser.helpers.parseUserId
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/forum/index.php?showuser=4575561
internal object ProfileLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Profile): LinkUrl {
        with(builder) {
            query(link.userId, UserIdCase.ShowUser)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Profile? {
        val userId = url.parseUserId(UserIdCase.ShowUser) ?: return null
        return Links.Board.Profile(userId = userId)
    }

}