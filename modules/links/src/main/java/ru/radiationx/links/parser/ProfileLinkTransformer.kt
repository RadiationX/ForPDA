package ru.radiationx.links.parser

import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter

//https://4pda.to/forum/index.php?showuser=4575561
class ProfileLinkTransformer {

    fun build(builder: LinkBuilderAdapter, link: Links.Board.Profile): LinkUrlAdapter {
        with(builder) {
            query("showuser", link.userId.id)
        }
        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.Board.Profile? {
        val userId = url.query("showuser")?.toIntOrNull()?.let { UserId(it) } ?: return null
        return Links.Board.Profile(userId = userId)
    }

}