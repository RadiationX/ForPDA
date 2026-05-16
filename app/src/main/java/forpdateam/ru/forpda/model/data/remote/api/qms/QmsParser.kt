package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class QmsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Qms

    fun parseSearch(response: String): List<ForumUser> = patternProvider
        .getRegexParser(scope.scope, scope.finduser)
        .map(response) { matcher ->
            ForumUser.required(
                id = matcher.require(1).toInt(),
                nick = matcher.require(2).fromHtml(),
                avatar = matcher.require(3).let {
                    when {
                        it.substring(0, 2) == "//" -> "https:$it"
                        it.substring(0, 1) == "/" -> "https://4pda.to$it"
                        else -> it
                    }
                }
            )
        }

    fun parseBlackList(response: String): List<QmsContact> {
        checkOperation(response)
        return patternProvider
            .getRegexParser(scope.scope, scope.blacklist_main)
            .map(response) { matcher ->
                QmsContact(
                    user = ForumUser.required(
                        id = matcher.require(1).toInt(),
                        nick = matcher.require(3).fromHtml(),
                        avatar = matcher.require(2)
                    ),
                    count = 0
                )
            }
    }

    private fun checkOperation(response: String) = patternProvider
        .getRegexParser(scope.scope, scope.blacklist_msg)
        .findAll(response) { matcher ->
            if (!matcher.require(1).contains("success")) {
                throw Exception(matcher.require(2).trim().fromHtml())
            }
        }

    fun parseContacts(response: String): List<QmsContact> = patternProvider
        .getRegexParser(scope.scope, scope.contacts_main)
        .map(response) { matcher ->
            QmsContact(
                user = ForumUser.required(
                    id = matcher.require(1).toInt(),
                    nick = matcher.require(4).trim().fromHtml(),
                    avatar = matcher.require(3)
                ),
                count = matcher.get(2).asCount()
            )
        }

    fun parseThemes(response: String, argId: Int): QmsThemes {
        val nick = patternProvider
            .getRegexParser(scope.scope, scope.thread_nick)
            .requireOnce(response) { matcher ->
                matcher.require(1).fromHtml()
            }

        val themes = patternProvider
            .getRegexParser(scope.scope, scope.thread_main)
            .map(response) { matcher ->
                QmsTheme(
                    id = matcher.require(1).toInt(),
                    date = matcher.require(2),
                    name = matcher.require(3).trim().fromHtml(),
                    countMessages = matcher.require(4).toInt(),
                    countNew = matcher.get(5).asCount(),
                )
            }

        return QmsThemes(User.required(argId, nick), themes)
    }

    fun parseChat(response: String): QmsChatModel {
        val chat = patternProvider
            .getRegexParser(scope.scope, scope.chat_info)
            .mapOnce(response) { matcher ->
                QmsChatModel(
                    title = matcher.require(2).trim().fromHtml(),
                    themeId = matcher.require(4).toInt(),
                    user = ForumUser.required(
                        id = matcher.require(3).toInt(),
                        nick = matcher.require(1).trim().fromHtml(),
                        avatar = matcher.require(5),
                    ),
                    messages = localParseMessages(response),
                    showedMessIndex = 0,
                    html = null
                )
            }
        return requireNotNull(chat) {
            "Can't parse chat"
        }
    }

    fun sendMessage(response: String): List<QmsMessage> {
        patternProvider
            .getRegexParser(scope.scope, scope.send_message_error)
            .findOnce(response) {
                throw Exception(it.require(1).trim())
            }
        return localParseMessages(response)
    }

    fun parseMoreMessages(response: String): List<QmsMessage> = localParseMessages(response)

    fun parseUserFromWebSocket(response: String): Int = patternProvider
        .getRegexParser(scope.scope, scope.message_info)
        .mapOnce(response) {
            it.require(1).toInt()
        }
        ?: 0

    private fun localParseMessages(response: String): List<QmsMessage> = patternProvider
        .getRegexParser(scope.scope, scope.chat_pattern)
        .map(response) { matcher ->
            if (matcher.get(1) == null && matcher.get(7) != null) {
                QmsMessage.Date(date = matcher.require(7).trim())
            } else {
                val isMyMessage = matcher.require(1).isNotEmpty()
                QmsMessage.Regular(
                    isMyMessage = isMyMessage,
                    id = matcher.require(2).toInt(),
                    readStatus = if (isMyMessage) {
                        matcher.require(3) != "1"
                    } else {
                        true
                    },
                    time = matcher.require(4),
                    avatar = matcher.require(5),
                    content = matcher.require(6).trim()
                )
            }
        }

    private fun String?.asCount(): Int {
        return this?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
    }
}
