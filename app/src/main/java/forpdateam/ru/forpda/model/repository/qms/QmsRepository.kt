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

/**
 * Created by radiationx on 01.01.18.
 */

class QmsRepository(
    private val qmsApi: QmsApi,
    private val attachmentsApi: AttachmentsApi,
    private val qmsCache: QmsCache,
    private val forumUsersCache: ForumUsersCache
) {

    fun observeContacts(): Flow<List<QmsContact>> {
        return qmsCache.observeContacts()
    }

    fun observeThemes(userId: Int): Flow<QmsThemes?> {
        return qmsCache.observeThemes(userId)
    }

    //Common
    suspend fun findUser(nick: String): List<ForumUser> {
        return qmsApi.findUser(nick)
    }

    suspend fun blockUser(nick: String): List<QmsContact> {
        return qmsApi.blockUser(nick)
    }

    suspend fun unBlockUsers(userId: Int): List<QmsContact> {
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

    suspend fun deleteDialog(mid: Int): String {
        return qmsApi.deleteDialog(mid)
    }


    //Themes
    suspend fun getThemesList(id: Int): QmsThemes {
        return qmsApi.getThemesList(id).let {
            qmsCache.saveThemes(it)
            qmsCache.getThemes(it.user.id)
        }
    }

    suspend fun deleteTheme(id: Int, themeId: Int): QmsThemes {
        return qmsApi.deleteTheme(id, themeId).let {
            qmsCache.saveThemes(it)
            qmsCache.getThemes(it.user.id)
        }
    }


    //Chat
    suspend fun getChat(userId: Int, themeId: Int): QmsChatModel {
        return qmsApi.getChat(userId, themeId)
    }

    suspend fun sendNewTheme(
        nick: String,
        title: String,
        mess: String,
        files: List<AttachmentItem>
    ): QmsChatModel {
        return qmsApi.sendNewTheme(nick, title, mess, files)
    }

    suspend fun sendMessage(
        userId: Int,
        themeId: Int,
        text: String,
        files: List<AttachmentItem>
    ): List<QmsMessage> {
        return qmsApi.sendMessage(userId, themeId, text, files)
    }

    suspend fun getMessagesFromWs(
        themeId: Int,
        messageId: Int,
        afterMessageId: Int
    ): List<QmsMessage> {
        return qmsApi.getMessagesFromWs(themeId, messageId, afterMessageId)
    }

    suspend fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): List<QmsMessage> {
        return qmsApi.getMessagesAfter(userId, themeId, afterMessageId)
    }

    suspend fun uploadFiles(
        files: List<RequestFile>,
        pending: List<AttachmentItem>
    ): List<AttachmentItem> {
        return attachmentsApi.uploadQmsFiles(files, pending)
    }
}
