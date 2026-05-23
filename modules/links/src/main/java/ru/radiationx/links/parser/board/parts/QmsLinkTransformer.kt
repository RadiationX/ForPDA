package ru.radiationx.links.parser.board.parts

import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter

//https://4pda.to/forum/index.php?act=qms
//https://4pda.to/forum/index.php?act=qms&mid=7898206
//https://4pda.to/forum/index.php?act=qms&mid=7898206&t=9391335
//https://4pda.to/forum/index.php?act=qms&action=create-thread
//https://4pda.to/forum/index.php?act=qms&action=create-thread&mid=7898206
//https://4pda.to/forum/index.php?act=qms&settings=blacklist
//unsupported
//https://4pda.to/forum/index.php?act=qms&search=неофициальный
class QmsLinkTransformer {

    fun build(builder: LinkBuilderAdapter, link: Links.Board.Qms): LinkUrlAdapter {
        with(builder) {
            query("act", "qms")
            when (link) {
                Links.Board.Qms.Contacts -> Unit
                is Links.Board.Qms.Threads -> {
                    query("mid", link.userId.id)
                }

                is Links.Board.Qms.Chat -> {
                    query("mid", link.chatId.userId.id)
                    query("t", link.chatId.threadId.id)
                }

                is Links.Board.Qms.CreateThread -> {
                    query("action", "create-thread")
                    link.userId?.also { query("mid", it.id) }
                }

                Links.Board.Qms.BlackList -> {
                    query("settings", "blacklist")
                }
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.Board.Qms? {
        if (url.query("act") != "qms") return null

        if (url.query("settings") == "blacklist") {
            return Links.Board.Qms.BlackList
        }

        val userId = url.query("mid")?.toIntOrNull()?.let { UserId(it) }
        if (url.query("action") == "create-thread") {
            return Links.Board.Qms.CreateThread(userId = userId)
        }

        val threadId = url.query("t")?.toIntOrNull()?.let { QmsThreadId(it) }

        if (userId != null && threadId != null) {
            return Links.Board.Qms.Chat(chatId = QmsChatId(userId, threadId))
        }

        if (userId != null) {
            return Links.Board.Qms.Threads(userId = userId)
        }

        return Links.Board.Qms.Contacts
    }

}