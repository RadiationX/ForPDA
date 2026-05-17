package forpdateam.ru.forpda.presentation.qms.chat

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.asRegular
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.events.WebSocketEventsRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsChatPresenter(
    private val qmsInteractor: QmsInteractor,
    private val avatarRepository: AvatarRepository,
    private val webSocketEventsRepository: WebSocketEventsRepository,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val templateManager: TemplateManager,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler
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
            .webViewFontSize
            .onEach {
                viewState.setFontSize(it)
            }
            .launchIn(viewModelScope)

        templateManager
            .observeThemeType()
            .onEach {
                viewState.setStyleType(it)
            }
            .launchIn(viewModelScope)
        webSocketEventsRepository
            .observeEvents()
            .filterIsInstance<WebSocketEvent.Qms>()
            .onEach {
                handleEvent(it)
            }
            .launchIn(viewModelScope)
        nick?.let { nick -> title?.let { title -> viewState.setTitles(title, nick) } }

        updateMode()
        if (currentMode == MODE_CHAT) {
            tryShowAvatar()
            loadChat()
        }
    }

    private fun updateMode() {
        currentMode =
            if (themeId == QmsChatModel.NOT_CREATED || userId == QmsChatModel.NOT_CREATED) {
                MODE_CREATING
            } else {
                MODE_CHAT
            }
        viewState.setChatMode(currentMode)
    }

    private fun updateCurrentData(newData: QmsChatModel) {
        currentData = newData
        themeId = newData.themeId
        userId = newData.user.id
        title = newData.title
        nick = newData.user.nick
        avatarUrl = newData.user.avatar
        updateMode()
    }

    fun findUser(nick: String) {
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.findUser(nick)
            }.onSuccess {
                viewState.onShowSearchRes(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun loadChat() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsInteractor.getChat(userId, themeId)
            }.onSuccess {
                updateCurrentData(it)
                viewState.showChat(it)
                initOnNewMessages(it)
                tryShowAvatar()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun sendNewTheme(nick: String, title: String, message: String, files: List<AttachmentItem>) {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsInteractor.sendNewTheme(nick, title, message, files)
            }.onSuccess {
                updateCurrentData(it)
                viewState.showChat(it)
                viewState.onNewThemeCreate(it)
                initOnNewMessages(it)
                tryShowAvatar()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun sendMessage(message: String, files: List<AttachmentItem>) {
        viewModelScope.launch {
            viewState.setMessageRefreshing(true)
            coRunCatching {
                qmsInteractor.sendMessage(userId, themeId, message, files)
            }.onSuccess {
                viewState.onSentMessage(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setMessageRefreshing(false)
        }
    }

    fun blockUser() {
        val nick = currentData?.user?.nick ?: return
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.blockUser(nick)
            }.map {
                it.firstOrNull { it.user.nick == nick } != null
            }.onSuccess {
                viewState.onBlockUser(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun tryShowAvatar() {
        val result = avatarUrl ?: currentData?.user?.avatar
        if (result != null) {
            viewState.showAvatar(result)
        } else {
            currentData?.let {
                viewModelScope.launch {
                    coRunCatching {
                        avatarRepository.getAvatar(it.user.nick)
                    }.onSuccess {
                        viewState.showAvatar(it)
                    }.onFailure {
                        errorHandler.handle(it)
                    }
                }
            }
        }
    }


    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.uploadFiles(files, pending)
            }.onSuccess {
                viewState.onUploadFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun handleEvent(event: WebSocketEvent.Qms) {
        val currentThemeId = currentData?.themeId ?: return
        if (event.themeId != currentThemeId) {
            return
        }

        when (event.type) {
            is WebSocketEvent.Qms.Type.New -> {
                onNewWsMessage(themeId, event.type.messageId)
            }

            is WebSocketEvent.Qms.Type.Read -> {
                viewState.makeAllRead()
            }

            is WebSocketEvent.Qms.Type.ReadAll -> {
                viewState.makeAllRead()
            }

            WebSocketEvent.Qms.Type.Typing -> Unit
            WebSocketEvent.Qms.Type.Uploading -> Unit
        }
    }

    private fun onNewWsMessage(themeId: Int, messageId: Int) {
        currentData?.let {
            val lastMessId = it.messages.lastOrNull()?.asRegular()?.id ?: 0
            viewModelScope.launch {
                coRunCatching {
                    qmsInteractor.getMessagesFromWs(themeId, messageId, lastMessId)
                }.onSuccess {
                    onNewMessages(it)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    fun checkNewMessages() {
        currentData?.let {
            val lastMessId = it.messages.lastOrNull()?.asRegular()?.id ?: 0
            viewModelScope.launch {
                coRunCatching {
                    qmsInteractor.getMessagesAfter(themeId, it.themeId, lastMessId)
                }.onSuccess {
                    onNewMessages(it)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    private fun initOnNewMessages(data: QmsChatModel) {
        val end = data.messages.size
        val start = Math.max(end - 30, 0)
        val newData = data.copy(showedMessIndex = start)
        val newMessages = data.messages.subList(start, end).toList()
        currentData = newData
        viewState.onNewMessages(newMessages)
    }

    private fun onNewMessages(items: List<QmsMessage>) {
        currentData?.let { data ->
            val result = items.filter { new ->
                data.messages.find { it.asRegular()?.id != new.asRegular()?.id } != null
            }
            val newData = data.copy(
                messages = data.messages + result
            )
            currentData = newData
            viewState.onNewMessages(result)
        }
    }

    fun createThemeNote() {
        currentData?.let {
            val url = "https://4pda.to/forum/index.php?act=qms&mid=${it.user.id}&t=${it.themeId}"
            viewState.showCreateNote(it.title, it.user.nick, url)
        }
    }

    fun openProfile() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showuser=${it.user.id}", router)
        }
    }

    fun openDialogs() {
        currentData?.let {
            router.navigateTo(Screen.QmsThemes().apply {
                screenTitle = it.user.nick
                userId = it.user.id
                avatarUrl = it.user.avatar
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
            val newData = it.copy(showedMessIndex = startIndex)
            currentData = newData
            viewState.showMoreMessages(it.messages, startIndex, endIndex)
        }
    }
}
