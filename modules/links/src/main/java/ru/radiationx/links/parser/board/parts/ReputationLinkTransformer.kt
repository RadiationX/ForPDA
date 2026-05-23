package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Links
import ru.radiationx.links.parser.helpers.UserIdCase
import ru.radiationx.links.parser.helpers.parsePageOffset
import ru.radiationx.links.parser.helpers.parseUserId
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/forum/index.php?act=rep&view=history&mid=7898206&mode=to&order=asc
//https://4pda.to/forum/index.php?act=rep&view=history&mid=7898206&mode=from&order=desc&st=5000
//https://4pda.to/forum/index.php?act=rep&order=asc&st=5000
//https://4pda.to/forum/index.php?act=rep&view=rating&order=asc
internal object ReputationLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Links.Board.Reputation): LinkUrl {
        with(builder) {
            query("act", "rep")
            when (link) {
                is Links.Board.Reputation.History -> {
                    query("view", "history")
                    query(link.userId, UserIdCase.Reputation)
                    query(link.offset)
                    fillOrder(link.order)
                    val mode = when (link.mode) {
                        Links.Board.Reputation.History.Mode.From -> "from"
                        Links.Board.Reputation.History.Mode.To -> "to"
                    }
                    query("mode", mode)
                }

                is Links.Board.Reputation.Rating -> {
                    query("view", "rating")
                    query(link.offset)
                    fillOrder(link.order)
                }
            }
        }
        return builder.build()
    }

    private fun LinkUrlBuilder.fillOrder(order: Links.Board.Reputation.Order) {
        val orderValue = when (order) {
            Links.Board.Reputation.Order.Asc -> "asc"
            Links.Board.Reputation.Order.Desc -> "desc"
        }
        query("order", orderValue)
    }

    fun parse(url: LinkUrl): Links.Board.Reputation? {
        if (url.query("act") != "rep") return null

        parseHistory(url)?.also {
            return it
        }

        parseRating(url)?.also {
            return it
        }
        return null
    }

    private fun parseHistory(url: LinkUrl): Links.Board.Reputation.History? {
        val userId = url.parseUserId(UserIdCase.Reputation) ?: return null
        val mode = when (url.query("mode")) {
            "from" -> Links.Board.Reputation.History.Mode.From
            "to" -> Links.Board.Reputation.History.Mode.To
            else -> Links.Board.Reputation.History.Mode.To
        }
        val offset = url.parsePageOffset()
        val order = parseOrder(url)
        return Links.Board.Reputation.History(
            userId = userId,
            mode = mode,
            order = order,
            offset = offset
        )
    }

    private fun parseRating(url: LinkUrl): Links.Board.Reputation.Rating? {
        val view = url.query("view")
        if (view != null && view != "rating") return null
        val offset = url.parsePageOffset()
        val order = parseOrder(url)
        return Links.Board.Reputation.Rating(order = order, offset = offset)
    }

    private fun parseOrder(url: LinkUrl): Links.Board.Reputation.Order {
        return when (url.query("order")) {
            "asc" -> Links.Board.Reputation.Order.Asc
            "desc" -> Links.Board.Reputation.Order.Desc
            else -> Links.Board.Reputation.Order.Desc
        }
    }

}