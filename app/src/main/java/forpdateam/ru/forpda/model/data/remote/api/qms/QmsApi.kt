package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.net.URLEncoder
import javax.inject.Inject


/**
 * Created by radiationx on 29.07.16.
 */
class QmsApi @Inject constructor(
    private val webClient: WebClient,
    private val qmsParser: QmsParser
) {

    suspend fun getBlackList(): List<QmsContact> {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&settings=blacklist")
            .formHeader("xhr", "body")
        val response = webClient.request(builder.build())
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun getContactList(): List<QmsContact> {
        val response = webClient.request(
            NetworkRequest.Builder()
                .url("https://4pda.to/forum/index.php?&act=qms-xhr&action=userlist").build()
        )
        return qmsParser.parseContacts(response.body)
    }

    suspend fun unBlockUsers(id: Int): List<QmsContact> {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
            .formHeader("action", "delete-users")
        val strId = Integer.toString(id)
        builder.formHeader("user-id[$strId]", strId)
        val response = webClient.request(builder.build())
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun blockUser(nick: String): List<QmsContact> {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&settings=blacklist&xhr=blacklist-form&do=1")
            .formHeader("action", "add-user")
            .formHeader("username", nick)
        val response = webClient.request(builder.build())
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun getThemesList(id: Int): QmsThemes {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&mid=$id")
            .formHeader("xhr", "body")
        val response = webClient.request(builder.build())
        return qmsParser.parseThemes(response.body, id)
    }

    suspend fun deleteTheme(id: Int, themeId: Int): QmsThemes {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&mid=$id&xhr=body&do=1")
            .formHeader("xhr", "body")
            .formHeader("action", "delete-threads")
            .formHeader("thread-id[$themeId]", themeId.toString())
        val response = webClient.request(builder.build())
        return qmsParser.parseThemes(response.body, id)
    }

    suspend fun getChat(userId: Int, themeId: Int): QmsChatModel {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&mid=$userId&t=$themeId")
            .formHeader("xhr", "body")
        val response = webClient.request(builder.build())
        return qmsParser.parseChat(response.body)
    }

    suspend fun findUser(nick: String): List<ForumUser> {
        val encodedNick = URLEncoder.encode(nick, "UTF-8")
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms-xhr&action=autocomplete-username&q=$encodedNick")
            .xhrHeader()
        val response = webClient.request(builder.build())
        return qmsParser.parseSearch(response.body)
    }

    suspend fun sendNewTheme(
        nick: String,
        title: String,
        mess: String,
        files: List<AttachmentItem>
    ): QmsChatModel {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms&action=create-thread&xhr=body&do=1")
            .formHeader("username", nick)
            .formHeader("title", title)
            .formHeader("message", mess)
            .formHeader("attaches", files.joinToString { it.id.toString() })
        val response = webClient.request(builder.build())
        return qmsParser.parseChat(response.body)
    }

    suspend fun sendMessage(
        userId: Int,
        themeId: Int,
        text: String,
        files: List<AttachmentItem>
    ): List<QmsMessage> {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php")
            .formHeader("act", "qms-xhr")
            .formHeader("action", "send-message")
            .formHeader("message", text)
            .formHeader("mid", Integer.toString(userId))
            .formHeader("t", Integer.toString(themeId))
            .formHeader("attaches", files.joinToString { it.id.toString() })
        val response = webClient.request(builder.build())
        return qmsParser.sendMessage(response.body)
    }

    suspend fun getMessagesFromWs(
        themeId: Int,
        messageId: Int,
        afterMessageId: Int
    ): List<QmsMessage> {
        val messInfoBuilder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms-xhr&")
            .formHeader("action", "message-info")
            .formHeader("t", Integer.toString(themeId))
            .formHeader("msg-id", Integer.toString(messageId))
        val messInfoResponse = webClient.request(messInfoBuilder.build())
        val userId = qmsParser.parseUserFromWebSocket(messInfoResponse.body)
        return getMessagesAfter(userId, themeId, afterMessageId)
    }

    suspend fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): List<QmsMessage> {
        val threadMessagesBuilder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php?act=qms-xhr&")
            .xhrHeader()
            .formHeader("action", "get-thread-messages")
            .formHeader("mid", Integer.toString(userId))
            .formHeader("t", Integer.toString(themeId))
            .formHeader("after-message", Integer.toString(afterMessageId))
        val response = webClient.request(threadMessagesBuilder.build())
        return qmsParser.parseMoreMessages(response.body)
    }

    suspend fun deleteDialog(mid: Int): String {
        val builder = NetworkRequest.Builder()
            .url("https://4pda.to/forum/index.php")
            .formHeader("act", "qms-xhr")
            .formHeader("action", "del-member")
            .formHeader("del-mid", Integer.toString(mid))
        return webClient.request(builder.build()).body
    }

}
