package ru.radiationx.links.parser.board.parts

import ru.radiationx.links.Link
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

    fun build(builder: LinkUrlBuilder, link: Link.Board.Reputation): LinkUrl {
        with(builder) {
            query("act", "rep")
            when (link) {
                is Link.Board.Reputation.History -> {
                    query("view", "history")
                    query(link.userId, UserIdCase.Reputation)
                    query(link.offset)
                    fillOrder(link.order)
                    val mode = when (link.mode) {
                        Link.Board.Reputation.History.Mode.From -> "from"
                        Link.Board.Reputation.History.Mode.To -> "to"
                    }
                    query("mode", mode)
                }

                is Link.Board.Reputation.Rating -> {
                    query("view", "rating")
                    query(link.offset)
                    fillOrder(link.order)
                }
            }
        }
        return builder.build()
    }

    private fun LinkUrlBuilder.fillOrder(order: Link.Board.Reputation.Order) {
        val orderValue = when (order) {
            Link.Board.Reputation.Order.Asc -> "asc"
            Link.Board.Reputation.Order.Desc -> "desc"
        }
        query("order", orderValue)
    }

    fun parse(url: LinkUrl): Link.Board.Reputation? {
        if (url.query("act") != "rep") return null

        parseHistory(url)?.also {
            return it
        }

        parseRating(url)?.also {
            return it
        }
        return null
    }

    private fun parseHistory(url: LinkUrl): Link.Board.Reputation.History? {
        val userId = url.parseUserId(UserIdCase.Reputation) ?: return null
        val mode = when (url.query("mode")) {
            "from" -> Link.Board.Reputation.History.Mode.From
            "to" -> Link.Board.Reputation.History.Mode.To
            else -> Link.Board.Reputation.History.Mode.To
        }
        val offset = url.parsePageOffset()
        val order = parseOrder(url)
        return Link.Board.Reputation.History(
            userId = userId,
            mode = mode,
            order = order,
            offset = offset
        )
    }

    private fun parseRating(url: LinkUrl): Link.Board.Reputation.Rating? {
        val view = url.query("view")
        if (view != null && view != "rating") return null
        val offset = url.parsePageOffset()
        val order = parseOrder(url)
        return Link.Board.Reputation.Rating(order = order, offset = offset)
    }

    private fun parseOrder(url: LinkUrl): Link.Board.Reputation.Order {
        return when (url.query("order")) {
            "asc" -> Link.Board.Reputation.Order.Asc
            "desc" -> Link.Board.Reputation.Order.Desc
            else -> Link.Board.Reputation.Order.Desc
        }
    }

}