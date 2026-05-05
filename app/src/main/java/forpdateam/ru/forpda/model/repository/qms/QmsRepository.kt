package forpdateam.ru.forpda.model.repository.qms

import android.util.Log
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.CountersHolder
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
    private val forumUsersCache: ForumUsersCache,
    private val countersHolder: CountersHolder
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

    suspend fun handleEvent(event: TabNotification) {
        if (!NotificationEvent.fromQms(event.source)) {
            return
        }
        val themesList = qmsCache.getAllThemes()
        val allContacts = qmsCache.getContacts()

        var targetTheme: QmsTheme? = null
        var targetDialog: QmsThemes? = null

        for (dialog in themesList) {
            for (theme in dialog.themes) {
                if (theme.id == event.event.sourceId) {
                    targetDialog = dialog
                    targetTheme = theme
                    break
                }
            }
            if (targetTheme != null) {
                break
            }
        }
        Log.d("kokoso", "$targetDialog : $targetTheme")

        if (targetDialog != null && targetTheme != null) {
            Log.d(
                "kokoso",
                "${event.isWebSocket}, ${event.type}, ${event.source}, ${event.event.msgCount}"
            )

            val newThemeCount = when {
                NotificationEvent.isRead(event.type) -> 0
                NotificationEvent.isNew(event.type) -> if (event.isWebSocket) {
                    targetTheme.countNew + 1
                } else {
                    event.event.msgCount
                }

                else -> targetTheme.countNew
            }

            val updatedThemes = targetDialog.themes.map {
                if (it == targetTheme) {
                    it.copy(countNew = newThemeCount)
                } else {
                    it
                }
            }
            val updatedDialog = targetDialog.copy(themes = updatedThemes)
            qmsCache.saveThemes(updatedDialog)

            allContacts.firstOrNull { it.user.id == targetDialog.user.id }?.let { contact ->
                val newContactCount = targetDialog.themes.sumOf { it.countNew }
                Log.d("kokoso", "upd contact cound ${contact.count} to $newContactCount")
                val newContact = contact.copy(count = newContactCount)
                qmsCache.updateContact(newContact)
            }
        }

        val newCounters = countersHolder.get().copy(
            qms = if (event.isWebSocket) {
                allContacts.sumOf { it.count }
            } else {
                event.loadedEvents.sumOf { it.msgCount }
            }
        )
        countersHolder.set(newCounters)
    }
}
