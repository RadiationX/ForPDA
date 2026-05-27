package forpdateam.ru.forpda.model.repository.qms

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import kotlinx.coroutines.flow.Flow
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.UserId
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class QmsRepository @Inject constructor(
    private val qmsApi: QmsApi,
    private val attachmentsApi: AttachmentsApi,
    private val qmsCache: QmsCache,
    private val forumUsersCache: ForumUsersCache
) {

    fun observeContacts(): Flow<List<QmsContact>> {
        return qmsCache.observeContacts()
    }

    fun observeThemes(userId: UserId): Flow<QmsThemes?> {
        return qmsCache.observeThemes(userId)
    }

    fun observeContact(userId: UserId): Flow<QmsContact?> {
        return qmsCache.observeContact(userId)
    }

    //Common
    suspend fun findUserById(userId: UserId): ForumUser? {
        return qmsCache.getContact(userId)?.user ?: forumUsersCache.getUserById(userId)
    }

    suspend fun findUser(nick: String): List<ForumUser> {
        return qmsApi.findUser(nick)
    }

    suspend fun blockUser(nick: String): List<QmsContact> {
        return qmsApi.blockUser(nick)
    }

    suspend fun unBlockUsers(userId: UserId): List<QmsContact> {
        return qmsApi.unBlockUsers(userId)
    }

    //Contacts
    suspend fun getContactList(): List<QmsContact> {
        return qmsApi.getContactList().let { contacts ->
            forumUsersCache.saveUsers(contacts.map { it.user })
            qmsCache.saveContacts(contacts)
            qmsCache.getContacts()
        }
    }

    suspend fun getBlackList(): List<QmsContact> {
        return qmsApi.getBlackList()
    }

    suspend fun deleteDialog(userId: UserId): String {
        return qmsApi.deleteDialog(userId)
    }


    //Themes
    suspend fun getThemesList(userId: UserId): QmsThemes {
        if (qmsCache.getContact(userId) == null) {
            getContactList()
        }
        return qmsApi.getThemesList(userId).let {
            qmsCache.saveThemes(it)
            qmsCache.getThemes(it.user.id)
        }
    }

    suspend fun deleteTheme(chatId: QmsChatId): QmsThemes {
        return qmsApi.deleteTheme(chatId).let {
            qmsCache.saveThemes(it)
            qmsCache.getThemes(it.user.id)
        }
    }


    //Chat
    suspend fun getChat(chatId: QmsChatId): QmsChatModel {
        return qmsApi.getChat(chatId)
    }

    suspend fun sendNewTheme(nick: String, title: String, mess: String, files: List<AttachmentItem>): QmsChatModel {
        return qmsApi.sendNewTheme(nick, title, mess, files)
    }

    suspend fun sendMessage(chatId: QmsChatId, text: String, files: List<AttachmentItem>): List<QmsMessage> {
        return qmsApi.sendMessage(chatId, text, files)
    }

    suspend fun getMessagesFromWs(themeId: QmsThreadId, messageId: QmsMessageId, afterMessageId: QmsMessageId?): List<QmsMessage> {
        return qmsApi.getMessagesFromWs(themeId, messageId, afterMessageId)
    }

    suspend fun getMessagesAfter(chatId: QmsChatId, afterMessageId: QmsMessageId?): List<QmsMessage> {
        return qmsApi.getMessagesAfter(chatId, afterMessageId)
    }

    suspend fun uploadFiles(
        files: List<RequestFile>,
        pending: List<AttachmentItem>
    ): List<AttachmentItem> {
        return attachmentsApi.uploadQmsFiles(files, pending)
    }

}
