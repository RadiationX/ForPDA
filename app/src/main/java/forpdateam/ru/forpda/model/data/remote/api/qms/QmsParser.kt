package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.extensions.findAll
import forpdateam.ru.forpda.extensions.findOnce
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class QmsParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Qms

    fun parseSearch(response: String): List<ForumUser> = patternProvider
        .getPattern(scope.scope, scope.finduser)
        .matcher(response)
        .map { matcher ->
            ForumUser.required(
                id = matcher.group(1).toInt(),
                nick = matcher.group(2).fromHtml(),
                avatar = matcher.group(3)?.let {
                    when {
                        it.substring(0, 2) == "//" -> "https:$it"
                        it.substring(0, 1) == "/" -> "https://4pda.to$it"
                        else -> it
                    }
                }
            )
        }

    fun parseBlackList(response: String): List<QmsContact> = response
        .also { checkOperation(it) }
        .let {
            patternProvider
                .getPattern(scope.scope, scope.blacklist_main)
                .matcher(it)
                .map { matcher ->
                    QmsContact(
                        user = ForumUser.required(
                            id = matcher.group(1).toInt(),
                            nick = matcher.group(3).fromHtml(),
                            avatar = matcher.group(2)
                        ),
                        count = 0
                    )
                }
        }

    private fun checkOperation(response: String) = patternProvider
        .getPattern(scope.scope, scope.blacklist_msg)
        .matcher(response)
        .findAll { matcher ->
            if (!matcher.group(1).contains("success")) {
                throw Exception(matcher.group(2).trim().fromHtml())
            }
        }

    fun parseContacts(response: String): List<QmsContact> = patternProvider
        .getPattern(scope.scope, scope.contacts_main)
        .matcher(response)
        .map { matcher ->
            QmsContact(
                user = ForumUser.required(
                    id = matcher.group(1).toInt(),
                    nick = ApiUtils.fromHtml(matcher.group(4).trim()),
                    avatar = matcher.group(3)
                ),
                count = matcher.group(2).asCount()
            )
        }

    fun parseThemes(response: String, argId: Int): QmsThemes {
        val nick = patternProvider
            .getPattern(scope.scope, scope.thread_nick)
            .matcher(response)
            .mapOnce { matcher ->
                matcher.group(1).fromHtml()
            }

        val themes = patternProvider
            .getPattern(scope.scope, scope.thread_main)
            .matcher(response)
            .map { matcher ->
                QmsTheme(
                    id = matcher.group(1).toInt(),
                    date = matcher.group(2),
                    name = matcher.group(3).trim().fromHtml(),
                    countMessages = matcher.group(4).toInt(),
                    countNew = matcher.group(5).asCount(),
                )
            }

        return QmsThemes(User.required(argId, nick), themes)
    }

    fun parseChat(response: String): QmsChatModel {
        val chat = patternProvider
            .getPattern(scope.scope, scope.chat_info)
            .matcher(response)
            .mapOnce { matcher ->
                QmsChatModel(
                    title = matcher.group(2).trim().fromHtml()!!,
                    themeId = matcher.group(4).toInt(),
                    user = ForumUser.required(
                        id = matcher.group(3).toInt(),
                        nick = matcher.group(1).trim().fromHtml(),
                        avatar = matcher.group(5),
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

    fun sendMessage(response: String): List<QmsMessage> = response
        .also {
            patternProvider
                .getPattern(scope.scope, scope.send_message_error)
                .matcher(it)
                .findOnce {
                    throw Exception(it.group(1).trim())
                }
        }
        .let {
            localParseMessages(it)
        }

    fun parseMoreMessages(response: String): List<QmsMessage> = localParseMessages(response)

    fun parseUserFromWebSocket(response: String): Int = patternProvider
        .getPattern(scope.scope, scope.message_info)
        .matcher(response)
        .mapOnce {
            it.group(1).toInt()
        } ?: 0

    private fun localParseMessages(response: String): List<QmsMessage> = patternProvider
        .getPattern(scope.scope, scope.chat_pattern)
        .matcher(response)
        .map { matcher ->
            if (matcher.group(1) == null && matcher.group(7) != null) {
                QmsMessage.Date(date = matcher.group(7).trim())
            } else {
                val isMyMessage = matcher.group(1).isNotEmpty()
                QmsMessage.Regular(
                    isMyMessage = isMyMessage,
                    id = matcher.group(2).toInt(),
                    readStatus = if (isMyMessage) {
                        matcher.group(3) != "1"
                    } else {
                        true
                    },
                    time = matcher.group(4),
                    avatar = matcher.group(5),
                    content = matcher.group(6).trim()
                )
            }
        }

    private fun String?.asCount(): Int {
        return this?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
    }
}
