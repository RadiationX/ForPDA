package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject


/**
 * Created by radiationx on 29.07.16.
 */
class QmsApi @Inject constructor(
    private val webClient: WebClient,
    private val qmsParser: QmsParser
) {

    suspend fun getBlackList(): List<QmsContact> {
        val response = webClient.request(ApiRequest.Forum.Qms.GetBlackList)
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun getContactList(): List<QmsContact> {
        val response = webClient.request(ApiRequest.Forum.Qms.GetContacts)
        return qmsParser.parseContacts(response.body)
    }

    suspend fun unBlockUsers(id: Int): List<QmsContact> {
        val response = webClient.request(ApiRequest.Forum.Qms.UnblockUser(id))
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun blockUser(nick: String): List<QmsContact> {
        val response = webClient.request(ApiRequest.Forum.Qms.BlockUser(nick))
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun getThemesList(id: Int): QmsThemes {
        val response = webClient.request(ApiRequest.Forum.Qms.GetThreads(id))
        return qmsParser.parseThemes(response.body, id)
    }

    suspend fun deleteTheme(id: Int, themeId: Int): QmsThemes {
        val response = webClient.request(ApiRequest.Forum.Qms.DeleteThread(id, themeId))
        return qmsParser.parseThemes(response.body, id)
    }

    suspend fun getChat(userId: Int, themeId: Int): QmsChatModel {
        val response = webClient.request(ApiRequest.Forum.Qms.GetChat(userId, themeId))
        return qmsParser.parseChat(response.body)
    }

    suspend fun findUser(nick: String): List<ForumUser> {
        val response = webClient.request(ApiRequest.Forum.Qms.FindUser(nick))
        return qmsParser.parseSearch(response.body)
    }

    suspend fun sendNewTheme(
        nick: String,
        title: String,
        mess: String,
        files: List<AttachmentItem>
    ): QmsChatModel {
        val response = webClient.request(ApiRequest.Forum.Qms.CreateThread(nick, title, mess, files.map { it.id }))
        return qmsParser.parseChat(response.body)
    }

    suspend fun sendMessage(
        userId: Int,
        themeId: Int,
        text: String,
        files: List<AttachmentItem>
    ): List<QmsMessage> {
        val response = webClient.request(ApiRequest.Forum.Qms.SendMessage(userId, themeId, text, files.map { it.id }))
        return qmsParser.sendMessage(response.body)
    }

    suspend fun getMessagesFromWs(
        themeId: Int,
        messageId: Int,
        afterMessageId: Int
    ): List<QmsMessage> {
        val messInfoResponse = webClient.request(ApiRequest.Forum.Qms.GetMessageInfo(themeId, messageId, afterMessageId))
        val userId = qmsParser.parseUserFromWebSocket(messInfoResponse.body)
        return getMessagesAfter(userId, themeId, afterMessageId)
    }

    suspend fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): List<QmsMessage> {
        val response = webClient.request(ApiRequest.Forum.Qms.GetMessagesAfter(userId, themeId, afterMessageId))
        return qmsParser.parseMoreMessages(response.body)
    }

    suspend fun deleteDialog(mid: Int): String {
        return webClient.request(ApiRequest.Forum.Qms.DeleteThreads(mid)).body
    }

}
