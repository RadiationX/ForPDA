package forpdateam.ru.forpda.presentation.qms.chat

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.TemplateManager

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsChatPresenter(
        private val qmsInteractor: QmsInteractor,
        private val qmsChatTemplate: QmsChatTemplate,
        private val avatarRepository: AvatarRepository,
        private val eventsRepository: EventsRepository,
        private val mainPreferencesHolder: MainPreferencesHolder,
        private val templateManager: TemplateManager,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<QmsChatView>(), IQmsChatPresenter {

    companion object {
        const val MODE_CHAT = "chat"
        const val MODE_CREATING = "creating"
    }

    var themeId = 0
    var userId = 0
    var title: String? = null
    var nick: String? = null
    var avatarUrl: String? = null

    private var currentMode = MODE_CHAT

    private var currentData: QmsChatModel? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        mainPreferencesHolder
                .observeWebViewFontSize()
                .subscribe {
                    viewState.setFontSize(it)
                }
                .untilDestroy()

        templateManager
                .observeThemeType()
                .subscribe {
                    viewState.setStyleType(it)
                }
                .untilDestroy()
        eventsRepository
                .observeEventsTab()
                .subscribe {
                    handleEvent(it)
                }
                .untilDestroy()
        nick?.let { nick -> title?.let { title -> viewState.setTitles(title, nick) } }

        updateMode()
        if (currentMode == MODE_CHAT) {
            tryShowAvatar()
            loadChat()
        }
    }

    private fun updateMode() {
        currentMode = if (themeId == QmsChatModel.NOT_CREATED || userId == QmsChatModel.NOT_CREATED) {
            MODE_CREATING
        } else {
            MODE_CHAT
        }
        viewState.setChatMode(currentMode)
    }

    private fun updateCurrentData(newData: QmsChatModel) {
        currentData = newData
        themeId = newData.themeId
        userId = newData.userId
        title = newData.title
        nick = newData.nick
        avatarUrl = newData.avatarUrl
        updateMode()
    }

    fun findUser(nick: String) {
        qmsInteractor
                .findUser(nick)
                .subscribe({
                    viewState.onShowSearchRes(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun loadChat() {
        qmsInteractor
                .getChat(userId, themeId)
                //.map { qmsChatTemplate.mapEntity(it) }
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    updateCurrentData(it)
                    viewState.showChat(it)
                    initOnNewMessages(it)
                    tryShowAvatar()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun sendNewTheme(nick: String, title: String, message: String, files: List<AttachmentItem>) {
        qmsInteractor
                .sendNewTheme(nick, title, message, files)
                //.map { qmsChatTemplate.mapEntity(it) }
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    updateCurrentData(it)
                    viewState.showChat(it)
                    viewState.onNewThemeCreate(it)
                    initOnNewMessages(it)
                    tryShowAvatar()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun sendMessage(message: String, files: List<AttachmentItem>) {
        qmsInteractor
                .sendMessage(userId, themeId, message, files)
                .doOnSubscribe { viewState.setMessageRefreshing(true) }
                .doAfterTerminate { viewState.setMessageRefreshing(false) }
                .subscribe({
                    viewState.onSentMessage(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun blockUser() {
        currentData?.nick?.let { nick ->
            qmsInteractor
                    .blockUser(nick)
                    .map { it.firstOrNull { it.nick == nick } != null }
                    .subscribe({
                        viewState.onBlockUser(it)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    private fun tryShowAvatar() {
        val result = avatarUrl?.let { it } ?: currentData?.avatarUrl?.let { it }
        if (result != null) {
            viewState.showAvatar(result)
        } else {
            currentData?.let {
                avatarRepository
                        .getAvatar(it.nick.orEmpty())
                        .subscribe({
                            viewState.showAvatar(it)
                        }, {
                            errorHandler.handle(it)
                        })
                        .untilDestroy()
            }
        }
    }


    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        qmsInteractor
                .uploadFiles(files, pending)
                .subscribe({
                    viewState.onUploadFiles(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun handleEvent(event: TabNotification) {
        val themeId = event.event.sourceId
        val messageId = event.event.messageId
        currentData?.let {
            if (themeId == it.themeId) {
                when (event.type) {
                    NotificationEvent.Type.NEW -> {
                        onNewWsMessage(themeId, messageId)
                    }
                    NotificationEvent.Type.READ -> {
                        viewState.makeAllRead()
                    }
                    NotificationEvent.Type.MENTION -> {
                    }
                    NotificationEvent.Type.HAT_EDITED -> {
                    }
                    null -> {
                    }
                }
            }
        }

    }

    private fun onNewWsMessage(themeId: Int, messageId: Int) {
        currentData?.let {
            val lastMessId = it.messages.lastOrNull()?.id ?: 0
            qmsInteractor
                    .getMessagesFromWs(themeId, messageId, lastMessId)
                    .subscribe({
                        onNewMessages(it)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    fun checkNewMessages() {
        currentData?.let {
            val lastMessId = it.messages.lastOrNull()?.id ?: 0
            qmsInteractor
                    .getMessagesAfter(themeId, it.themeId, lastMessId)
                    .subscribe({
                        onNewMessages(it)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    private fun initOnNewMessages(data: QmsChatModel) {
        val end = data.messages.size
        val start = Math.max(end - 30, 0)
        data.showedMessIndex = start
        val newMessages = data.messages.subList(start, end).toList()
        viewState.onNewMessages(newMessages)
    }

    private fun onNewMessages(items: List<QmsMessage>) {
        currentData?.let { data ->
            val result = items.filter { new ->
                data.messages.find { it.id != new.id } != null
            }
            data.messages.addAll(result)
            viewState.onNewMessages(result)
        }
    }

    fun createThemeNote() {
        currentData?.let {
            val url = "https://4pda.ru/forum/index.php?act=qms&mid=${it.userId}&t=${it.themeId}"
            viewState.showCreateNote(it.title.orEmpty(), it.nick.orEmpty(), url)
        }
    }

    fun openProfile() {
        currentData?.let {
            linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${it.userId}", router)
        }
    }

    fun openDialogs() {
        currentData?.let {
            router.navigateTo(Screen.QmsThemes().apply {
                screenTitle = it.nick
                userId = it.userId
                avatarUrl = it.avatarUrl
            })
        }
    }

    fun onSendClick() {
        if (themeId == QmsChatModel.NOT_CREATED) {
            viewState.temp_sendNewTheme()
        } else {
            viewState.temp_sendMessage()
        }
    }

    override fun loadMoreMessages() {
        currentData?.let {
            val endIndex = it.showedMessIndex
            val startIndex = Math.max(endIndex - 30, 0)
            it.showedMessIndex = startIndex
            viewState.showMoreMessages(it.messages, startIndex, endIndex)
        }
    }
}
