package forpdateam.ru.forpda.model.data.remote.api.qms

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.WebClient
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.UserId
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

    suspend fun unBlockUsers(userId: UserId): List<QmsContact> {
        val response = webClient.request(ApiRequest.Forum.Qms.UnblockUser(userId))
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun blockUser(nick: String): List<QmsContact> {
        val response = webClient.request(ApiRequest.Forum.Qms.BlockUser(nick))
        return qmsParser.parseBlackList(response.body)
    }

    suspend fun getThemesList(userId: UserId): QmsThemes {
        val response = webClient.request(ApiRequest.Forum.Qms.GetThreads(userId))
        return qmsParser.parseThemes(response.body, userId)
    }

    suspend fun deleteTheme(chatId: QmsChatId): QmsThemes {
        val response = webClient.request(ApiRequest.Forum.Qms.DeleteThread(chatId))
        return qmsParser.parseThemes(response.body, chatId.userId)
    }

    suspend fun getChat(chatId: QmsChatId): QmsChatModel {
        val response = webClient.request(ApiRequest.Forum.Qms.GetChat(chatId))
        return qmsParser.parseChat(response.body)
    }

    suspend fun findUser(nick: String): List<ForumUser> {
        val response = webClient.request(ApiRequest.Forum.Qms.FindUser(nick))
        return qmsParser.parseSearch(response.body)
    }

    suspend fun sendNewTheme(nick: String, title: String, mess: String, files: List<AttachmentItem>): QmsChatModel {
        val response = webClient.request(ApiRequest.Forum.Qms.CreateThread(nick, title, mess, files.map { it.id }))
        return qmsParser.parseChat(response.body)
    }

    suspend fun sendMessage(chatId: QmsChatId, text: String, files: List<AttachmentItem>): List<QmsMessage> {
        val response = webClient.request(ApiRequest.Forum.Qms.SendMessage(chatId, text, files.map { it.id }))
        return qmsParser.sendMessage(response.body)
    }

    suspend fun getMessagesFromWs(themeId: QmsThreadId, messageId: QmsMessageId, afterMessageId: QmsMessageId?): List<QmsMessage> {
        val messInfoResponse = webClient.request(ApiRequest.Forum.Qms.GetMessageInfo(themeId, messageId))
        val userId = qmsParser.parseUserFromWebSocket(messInfoResponse.body)
        return getMessagesAfter(QmsChatId(UserId(userId), themeId), afterMessageId)
    }

    suspend fun getMessagesAfter(chatId: QmsChatId, afterMessageId: QmsMessageId?): List<QmsMessage> {
        val response = webClient.request(ApiRequest.Forum.Qms.GetMessagesAfter(chatId, afterMessageId))
        return qmsParser.parseMoreMessages(response.body)
    }

    suspend fun deleteDialog(userId: UserId): String {
        return webClient.request(ApiRequest.Forum.Qms.DeleteThreads(userId)).body
    }

}
