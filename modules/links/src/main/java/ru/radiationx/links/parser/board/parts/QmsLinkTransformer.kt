package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.links.Link
import ru.radiationx.links.parser.helpers.UserIdCase
import ru.radiationx.links.parser.helpers.parseUserId
import ru.radiationx.links.parser.helpers.query
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

//https://4pda.to/forum/index.php?act=qms
//https://4pda.to/forum/index.php?act=qms&mid=7898206
//https://4pda.to/forum/index.php?act=qms&mid=7898206&t=9391335
//https://4pda.to/forum/index.php?act=qms&action=create-thread
//https://4pda.to/forum/index.php?act=qms&action=create-thread&mid=7898206
//https://4pda.to/forum/index.php?act=qms&settings=blacklist
//unsupported
//https://4pda.to/forum/index.php?act=qms&search=неофициальный
internal object QmsLinkTransformer {

    fun build(builder: LinkUrlBuilder, link: Link.Board.Qms): LinkUrl {
        with(builder) {
            query("act", "qms")
            when (link) {
                Link.Board.Qms.Contacts -> Unit
                is Link.Board.Qms.Threads -> {
                    query(link.userId, UserIdCase.Qms)
                }

                is Link.Board.Qms.Chat -> {
                    query(link.chatId)
                }

                is Link.Board.Qms.CreateThread -> {
                    query("action", "create-thread")
                    link.userId?.also {
                        query(it, UserIdCase.Qms)
                    }
                }

                Link.Board.Qms.BlackList -> {
                    query("settings", "blacklist")
                }
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.Board.Qms? {
        if (url.query("act") != "qms") return null

        if (url.query("settings") == "blacklist") {
            return Link.Board.Qms.BlackList
        }

        val userId = url.parseUserId(UserIdCase.Qms)
        if (url.query("action") == "create-thread") {
            return Link.Board.Qms.CreateThread(userId = userId)
        }

        val threadId = url.parseQmsThreadId()

        if (userId != null && threadId != null) {
            return Link.Board.Qms.Chat(chatId = QmsChatId(userId, threadId))
        }

        if (userId != null) {
            return Link.Board.Qms.Threads(userId = userId)
        }

        return Link.Board.Qms.Contacts
    }


    private fun LinkUrl.parseQmsThreadId(): QmsThreadId? {
        return query("t")?.toIntOrNull()?.let { QmsThreadId(it) }
    }

    private fun LinkUrlBuilder.query(threadId: QmsThreadId) {
        query("t", threadId.id)
    }

    private fun LinkUrlBuilder.query(chatId: QmsChatId) {
        query(chatId.userId, UserIdCase.Qms)
        query(chatId.threadId)
    }

}