package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.PageOffset
import ru.radiationx.links.Links
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

    fun build(builder: LinkUrlBuilder, link: Links.Board.Favorite): LinkUrl {
        with(builder) {
            query("act", "fav")
            query("st", link.offset.value)

            val type = when (link.type) {
                Links.Board.Favorite.Type.All -> "all"
                Links.Board.Favorite.Type.Forums -> "forums"
                Links.Board.Favorite.Type.Topics -> "topic"
            }
            query("type", type)

            val sortKey = when (link.sort.key) {
                Links.Board.Favorite.Sort.Key.Title -> "title"
                Links.Board.Favorite.Sort.Key.LastPost -> "last_post"
            }
            query("sort_key", sortKey)

            val sortBy = when (link.sort.order) {
                Links.Board.Favorite.Sort.Order.Asc -> "A-Z"
                Links.Board.Favorite.Sort.Order.Desc -> "Z-A"
            }
            query("sort_by", sortBy)
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Links.Board.Favorite? {
        if (url.query("act") != "fav") return null
        val pageOffset = url.query("st")?.toIntOrNull()?.let { PageOffset(it) } ?: PageOffset.default
        val type = when (url.query("type")) {
            "all" -> Links.Board.Favorite.Type.All
            "topics" -> Links.Board.Favorite.Type.Topics
            "forums" -> Links.Board.Favorite.Type.Forums
            else -> Links.Board.Favorite.Type.All
        }
        val sortKey = when (url.query("sort_key")) {
            "title" -> Links.Board.Favorite.Sort.Key.Title
            "last_post" -> Links.Board.Favorite.Sort.Key.LastPost
            else -> Links.Board.Favorite.Sort.Key.LastPost
        }
        val sortOrder = when (url.query("sort_by")) {
            "A-Z" -> Links.Board.Favorite.Sort.Order.Asc
            "Z-A" -> Links.Board.Favorite.Sort.Order.Desc
            else -> Links.Board.Favorite.Sort.Order.Desc
        }

        return Links.Board.Favorite(
            offset = pageOffset,
            type = type,
            sort = Links.Board.Favorite.Sort(key = sortKey, order = sortOrder)
        )
    }

}