package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.*
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
                ForumUser().apply {
                    id = matcher.group(1).toInt()
                    nick = matcher.group(2).fromHtml().orEmpty()
                    avatar = matcher.group(3)?.let {
                        when {
                            it.substring(0, 2) == "//" -> "https:$it"
                            it.substring(0, 1) == "/" -> "https://4pda.to$it"
                            else -> it
                        }
                    }
                }
            }

    fun parseBlackList(response: String): List<QmsContact> = response
            .also { checkOperation(it) }
            .let {
                patternProvider
                        .getPattern(scope.scope, scope.blacklist_main)
                        .matcher(it)
                        .map { matcher ->
                            QmsContact().apply {
                                id = matcher.group(1).toInt()
                                avatar = matcher.group(2)
                                nick = matcher.group(3).fromHtml()
                            }
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
                QmsContact().apply {
                    id = matcher.group(1).toInt()
                    matcher.group(2).let {
                        count = if (it.isNullOrEmpty()) 0 else it.toInt()
                    }
                    avatar = matcher.group(3)
                    nick = ApiUtils.fromHtml(matcher.group(4).trim())
                }
            }

    fun parseThemes(response: String, argId: Int): QmsThemes = QmsThemes().also { data ->
        data.userId = argId
        patternProvider
                .getPattern(scope.scope, scope.thread_nick)
                .matcher(response)
                .findOnce { matcher ->
                    data.nick = matcher.group(1).fromHtml()
                }
        val list = patternProvider
                .getPattern(scope.scope, scope.thread_main)
                .matcher(response)
                .map { matcher ->
                    QmsTheme().apply {
                        id = matcher.group(1).toInt()
                        date = matcher.group(2)
                        name = matcher.group(3).trim().fromHtml()
                        countMessages = matcher.group(4).toInt()
                        matcher.group(5).also {
                            countNew = if (it.isNullOrEmpty()) 0 else it.toInt()
                        }
                    }
                }
        data.themes.addAll(list)
    }

    fun parseChat(response: String): QmsChatModel = QmsChatModel().also { data ->
        data.messages.addAll(localParseMessages(response))
        patternProvider
                .getPattern(scope.scope, scope.chat_info)
                .matcher(response)
                .findOnce { matcher ->
                    data.nick = matcher.group(1).trim().fromHtml()
                    data.title = matcher.group(2).trim().fromHtml()
                    data.userId = matcher.group(3).toInt()
                    data.themeId = matcher.group(4).toInt()
                    data.avatarUrl = matcher.group(5)
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
                QmsMessage().apply {
                    if (matcher.group(1) == null && matcher.group(7) != null) {
                        isDate = true
                        date = matcher.group(7).trim()
                    } else {
                        isMyMessage = !matcher.group(1).isEmpty()
                        id = matcher.group(2).toInt()
                        if (isMyMessage) {
                            readStatus = matcher.group(3) != "1"
                        } else {
                            readStatus = true
                        }
                        time = matcher.group(4)
                        avatar = matcher.group(5)
                        content = matcher.group(6).trim()
                    }
                }
            }

}
