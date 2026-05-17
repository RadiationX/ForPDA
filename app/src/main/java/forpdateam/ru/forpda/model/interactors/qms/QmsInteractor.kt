package forpdateam.ru.forpda.model.interactors.qms

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QmsInteractor @Inject constructor(
    private val qmsRepository: QmsRepository
) {

    fun observeContacts(): Flow<List<QmsContact>> {
        return qmsRepository.observeContacts()
    }

    fun observeContact(userId: Int): Flow<QmsContact?> {
        return qmsRepository.observeContact(userId)
    }

    fun observeThemes(userId: Int): Flow<QmsThemes?> {
        return qmsRepository.observeThemes(userId)
    }

    //Common
    suspend fun findUser(nick: String): List<ForumUser> {
        return qmsRepository.findUser(nick)
    }

    suspend fun blockUser(nick: String): List<QmsContact> {
        return qmsRepository.blockUser(nick)
    }

    suspend fun unBlockUsers(userId: Int): List<QmsContact> {
        return qmsRepository.unBlockUsers(userId)
    }

    //Contacts
    suspend fun getContactList(): List<QmsContact> {
        return qmsRepository.getContactList()
    }

    suspend fun getBlackList(): List<QmsContact> {
        return qmsRepository.getBlackList()
    }

    suspend fun deleteDialog(mid: Int): String {
        return qmsRepository.deleteDialog(mid)
    }

    //Themes
    suspend fun getThemesList(id: Int): QmsThemes {
        return qmsRepository.getThemesList(id)
    }

    suspend fun deleteTheme(id: Int, themeId: Int): QmsThemes {
        return qmsRepository.deleteTheme(id, themeId)
    }

    //Chat
    suspend fun getChat(userId: Int, themeId: Int): QmsChatModel {
        return qmsRepository.getChat(userId, themeId)
    }

    suspend fun sendNewTheme(
        nick: String,
        title: String,
        mess: String,
        files: List<AttachmentItem>
    ): QmsChatModel {
        return qmsRepository.sendNewTheme(nick, title, mess, files)
    }

    suspend fun sendMessage(
        userId: Int,
        themeId: Int,
        text: String,
        files: List<AttachmentItem>
    ): List<QmsMessage> {
        return qmsRepository.sendMessage(userId, themeId, text, files)
    }

    suspend fun getMessagesFromWs(
        themeId: Int,
        messageId: Int,
        afterMessageId: Int
    ): List<QmsMessage> {
        return qmsRepository.getMessagesFromWs(themeId, messageId, afterMessageId)
    }

    suspend fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): List<QmsMessage> {
        return qmsRepository.getMessagesAfter(userId, themeId, afterMessageId)
    }

    suspend fun uploadFiles(
        files: List<RequestFile>,
        pending: List<AttachmentItem>
    ): List<AttachmentItem> {
        return qmsRepository.uploadFiles(files, pending)
    }

}