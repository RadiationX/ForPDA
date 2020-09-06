package forpdateam.ru.forpda.model.interactors.qms

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class QmsInteractor(
        private val qmsRepository: QmsRepository,
        private val eventsRepository: EventsRepository
) {

    private var eventsDisposable: Disposable? = null

    fun subscribeEvents() {
        if (eventsDisposable != null) return
        eventsDisposable = eventsRepository
                .observeEventsTab()
                .subscribe {
                    qmsRepository.handleEvent(it)
                }
    }


    fun observeContacts(): Observable<List<QmsContact>> = qmsRepository
            .observeContacts()

    fun observeThemes(userId: Int): Observable<QmsThemes> = qmsRepository
            .observeThemes(userId)

    //Common
    fun findUser(nick: String): Single<List<ForumUser>> = qmsRepository
            .findUser(nick)

    fun blockUser(nick: String): Single<List<QmsContact>> = qmsRepository
            .blockUser(nick)

    fun unBlockUsers(userId: Int): Single<List<QmsContact>> = qmsRepository
            .unBlockUsers(userId)

    //Contacts
    fun getContactList(): Single<List<QmsContact>> = qmsRepository
            .getContactList()

    fun getBlackList(): Single<List<QmsContact>> = qmsRepository
            .getBlackList()

    fun deleteDialog(mid: Int): Single<String> = qmsRepository
            .deleteDialog(mid)

    //Themes
    fun getThemesList(id: Int): Single<QmsThemes> = qmsRepository
            .getThemesList(id)

    fun deleteTheme(id: Int, themeId: Int): Single<QmsThemes> = qmsRepository
            .deleteTheme(id, themeId)


    //Chat
    fun getChat(userId: Int, themeId: Int): Single<QmsChatModel> = qmsRepository
            .getChat(userId, themeId)

    fun sendNewTheme(nick: String, title: String, mess: String, files: List<AttachmentItem>): Single<QmsChatModel> = qmsRepository
            .sendNewTheme(nick, title, mess, files)

    fun sendMessage(userId: Int, themeId: Int, text: String, files: List<AttachmentItem>): Single<List<QmsMessage>> = qmsRepository
            .sendMessage(userId, themeId, text, files)

    fun getMessagesFromWs(themeId: Int, messageId: Int, afterMessageId: Int): Single<List<QmsMessage>> = qmsRepository
            .getMessagesFromWs(themeId, messageId, afterMessageId)

    fun getMessagesAfter(userId: Int, themeId: Int, afterMessageId: Int): Single<List<QmsMessage>> = qmsRepository
            .getMessagesAfter(userId, themeId, afterMessageId)

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>): Single<List<AttachmentItem>> = qmsRepository
            .uploadFiles(files, pending)

}