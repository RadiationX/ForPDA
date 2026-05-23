package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.LinkUrl

//https://4pda.to/forum/index.php?act=fav
//https://4pda.to/forum/index.php?act=fav&type=all
//https://4pda.to/forum/index.php?act=fav&type=topics
//https://4pda.to/forum/index.php?act=fav&type=forums
//https://4pda.to/forum/index.php?act=fav&st=30
//https://4pda.to/forum/index.php?act=fav&sort_key=title&sort_by=A-Z
//https://4pda.to/forum/index.php?act=fav&sort_key=last_post&sort_by=Z-A
internal object  FavoriteLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Favorite): LinkUrl {
        with(builder) {
            query("act", "fav")
            query(link.offset)

            val type = when (link.type) {
                Link.Board.Favorite.Type.All -> "all"
                Link.Board.Favorite.Type.Forums -> "forums"
                Link.Board.Favorite.Type.Topics -> "topic"
            }
            query("type", type)

            val sortKey = when (link.sort.key) {
                Link.Board.Favorite.Sort.Key.Title -> "title"
                Link.Board.Favorite.Sort.Key.LastPost -> "last_post"
            }
            query("sort_key", sortKey)

            val sortBy = when (link.sort.order) {
                Link.Board.Favorite.Sort.Order.Asc -> "A-Z"
                Link.Board.Favorite.Sort.Order.Desc -> "Z-A"
            }
            query("sort_by", sortBy)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Favorite? {
        if (url.query("act") != "fav") return null
        val pageOffset = url.parsePageOffset()
        val type = when (url.query("type")) {
            "all" -> Link.Board.Favorite.Type.All
            "topics" -> Link.Board.Favorite.Type.Topics
            "forums" -> Link.Board.Favorite.Type.Forums
            else -> Link.Board.Favorite.Type.All
        }
        val sortKey = when (url.query("sort_key")) {
            "title" -> Link.Board.Favorite.Sort.Key.Title
            "last_post" -> Link.Board.Favorite.Sort.Key.LastPost
            else -> Link.Board.Favorite.Sort.Key.LastPost
        }
        val sortOrder = when (url.query("sort_by")) {
            "A-Z" -> Link.Board.Favorite.Sort.Order.Asc
            "Z-A" -> Link.Board.Favorite.Sort.Order.Desc
            else -> Link.Board.Favorite.Sort.Order.Desc
        }

        return Link.Board.Favorite(
            offset = pageOffset,
            type = type,
            sort = Link.Board.Favorite.Sort(key = sortKey, order = sortOrder)
        )
    }

}