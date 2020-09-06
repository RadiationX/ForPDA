package forpdateam.ru.forpda.model.repository.qms

import android.util.Log
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.db.qms.QmsContactBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemeBd
import forpdateam.ru.forpda.entity.db.qms.QmsThemesBd
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.*
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.cache.qms.QmsCache
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.attachments.AttachmentsApi
import forpdateam.ru.forpda.model.data.remote.api.qms.QmsApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm

/**
 * Created by radiationx on 01.01.18.
 */

class QmsRepository(
        private val schedulers: SchedulersProvider,
        private val qmsApi: QmsApi,
        private val attachmentsApi: AttachmentsApi,
        private val qmsCache: QmsCache,
        private val forumUsersCache: ForumUsersCache,
        private val countersHolder: CountersHolder
) : BaseRepository(schedulers) {

    fun observeContacts(): Observable<List<QmsContact>> = qmsCache
            .observeContacts()
            .runInIoToUi()

    fun observeThemes(userId: Int): Observable<QmsThemes> = qmsCache
            .observeThemes(userId)
            .runInIoToUi()

    //Common
    fun findUser(nick: String): Single<List<ForumUser>> = Single
            .fromCallable { qmsApi.findUser(nick) }
            .runInIoToUi()

    fun blockUser(nick: String): Single<List<QmsContact>> = Single
            .fromCallable { qmsApi.blockUser(nick) }
            .runInIoToUi()

    fun unBlockUsers(userId: Int): Single<List<QmsContact>> = Single
            .fromCallable { qmsApi.unBlockUsers(userId) }
            .runInIoToUi()

    //Contacts
    fun getContactList(): Single<List<QmsContact>> = Single
            .fromCallable { qmsApi.getContactList() }
            .doOnSuccess { saveUsers(it) }
            .flatMap { saveContactsCache(it) }
            .runInIoToUi()

    fun getBlackList(): Single<List<QmsContact>> = Single
            .fromCallable { qmsApi.getBlackList() }
            .runInIoToUi()

    fun deleteDialog(mid: Int): Single<String> = Single
            .fromCallable { qmsApi.deleteDialog(mid) }
            .runInIoToUi()


    //Themes
    fun getThemesList(id: Int): Single<QmsThemes> = Single
            .fromCallable { qmsApi.getThemesList(id) }
            .flatMap { saveThemesCache(it) }
            .runInIoToUi()

    fun deleteTheme(id: Int, themeId: Int): Single<QmsThemes> = Single
            .fromCallable { qmsApi.deleteTheme(id, themeId) }
            .flatMap { saveThemesCache(it) }
            .runInIoToUi()


    //Chat
    fun getChat(userId: Int, themeId: Int): Single<QmsChatModel> = Single
            .fromCallable { qmsApi.getChat(userId, themeId) }
            .runInIoToUi()

    fun sendNewTheme(nick: String, title: String, mess: String, files: List<AttachmentItem>): Single<QmsChatModel> = Single
            .fromCallable { qmsApi.sendNewTheme(nick, title, mess, files) }
            .runInIoToUi()

    fun sendMessage(userId: Int, themeId: Int, text: String, files: List<AttachmentItem>): Single<List<QmsMessage>> = Single
            .fromCallable { qmsApi.sendMessage(userId, themeId, text, files) }
            .runInIoToUi()

    fun getMessagesFromWs(themeId: Int, messageId: Int, afterMessageId: Int): Single<List<QmsMessage>> = Single
            .fromCallable { qmsApi.getMessagesFromWs(themeId, messageId, afterMessageId) }
            .runInIoToUi()

    fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): Single<List<QmsMessage>> = Single
            .fromCallable { qmsApi.getMessagesAfter(userId, themeId, afterMessageId) }
            .runInIoToUi()

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>): Single<List<AttachmentItem>> = Single
            .fromCallable { attachmentsApi.uploadQmsFiles(files, pending) }
            .runInIoToUi()


    private fun saveUsers(contacts: List<QmsContact>) {
        val forumUsers = contacts.map { contact ->
            ForumUser().apply {
                id = contact.id
                nick = contact.nick
                avatar = contact.avatar
            }
        }
        forumUsersCache.saveUsers(forumUsers)
    }


    /*
    *
    * cache
    *
    * */

    private fun saveContactsCache(items: List<QmsContact>): Single<List<QmsContact>> = Single
            .fromCallable { qmsCache.saveContacts(items) }
            .flatMap { getContactsCache() }

    private fun getContactsCache(): Single<List<QmsContact>> = Single
            .fromCallable { qmsCache.getContacts() }

    private fun saveThemesCache(data: QmsThemes): Single<QmsThemes> = Single
            .fromCallable { qmsCache.saveThemes(data) }
            .flatMap { getThemesCache(data.userId) }

    private fun getThemesCache(userId: Int): Single<QmsThemes> = Single
            .fromCallable { qmsCache.getThemes(userId) }


    fun handleEvent(event: TabNotification) {
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
            Log.d("kokoso", "${event.isWebSocket}, ${event.type}, ${event.source}, ${event.event.msgCount}")
            if (event.isWebSocket) {
                if (NotificationEvent.isRead(event.type)) {
                    targetTheme.countNew = 0
                } else if (NotificationEvent.isNew(event.type)) {
                    targetTheme.countNew++
                }
            } else {
                if (NotificationEvent.isRead(event.type)) {
                    targetTheme.countNew = 0
                } else if (NotificationEvent.isNew(event.type)) {
                    targetTheme.countNew = event.event.msgCount
                }
            }


            qmsCache.saveThemes(targetDialog)
            allContacts.firstOrNull { it.id == targetDialog.userId }?.let { contact ->
                val newCount = targetDialog.themes.sumBy { it.countNew }
                Log.d("kokoso", "upd contact cound ${contact.count} to $newCount")
                contact.count = newCount

                qmsCache.updateContact(contact)
            }
        }

        countersHolder.set(countersHolder.get().also { counters ->
            if (event.isWebSocket) {
                counters.qms = allContacts.sumBy { it.count }
            } else {
                counters.qms = event.loadedEvents.sumBy { it.msgCount }
            }
        })
    }


}
