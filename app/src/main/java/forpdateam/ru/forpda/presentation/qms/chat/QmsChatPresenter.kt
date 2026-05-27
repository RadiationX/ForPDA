package forpdateam.ru.forpda.presentation.qms.chat

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.asRegular
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.events.WebSocketEventsRepository
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
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
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsMessageId
import ru.radiationx.coretypes.UserId
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 11.11.17.
 */

sealed interface QmsChatExtra : QuillExtra {
    data class Create(val userId: UserId?) : QmsChatExtra
    data class Existed(val chatId: QmsChatId) : QmsChatExtra
}

@InjectViewState
class QmsChatPresenter(
    private val argExtra: QmsChatExtra,
    private val qmsRepository: QmsRepository,
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

    private var currentMode = MODE_CHAT
    private var currentExtra = argExtra

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

        updateMode()
        when (argExtra) {
            is QmsChatExtra.Create -> loadUser(argExtra.userId)
            is QmsChatExtra.Existed -> loadChat(argExtra.chatId)
        }
    }

    private fun updateMode() {
        currentMode = if (currentExtra is QmsChatExtra.Existed) {
            MODE_CHAT
        } else {
            MODE_CREATING
        }
        viewState.setChatMode(currentMode)
    }

    private fun updateCurrentData(newData: QmsChatModel) {
        currentData = newData
        currentExtra = QmsChatExtra.Existed(newData.id)
        updateMode()
    }

    fun findUser(nick: String) {
        viewModelScope.launch {
            coRunCatching {
                qmsRepository.findUser(nick)
            }.onSuccess {
                viewState.onShowSearchRes(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun loadUser(userId: UserId?) {
        if (userId == null) return
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsRepository.findUserById(userId)
            }.onSuccess {
                if (it != null) {
                    viewState.showAvatar(it.avatar)
                    viewState.initNick(it.nick)
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun loadChat(chatId: QmsChatId) {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsRepository.getChat(chatId)
            }.onSuccess {
                updateCurrentData(it)
                viewState.showChat(it)
                initOnNewMessages(it)
                viewState.showAvatar(it.user.avatar)
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
                qmsRepository.sendNewTheme(nick, title, message, files)
            }.onSuccess {
                updateCurrentData(it)
                viewState.showChat(it)
                viewState.onNewThemeCreate(it)
                initOnNewMessages(it)
                viewState.showAvatar(it.user.avatar)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun sendMessage(message: String, files: List<AttachmentItem>) {
        val chat = currentData ?: return
        viewModelScope.launch {
            viewState.setMessageRefreshing(true)
            coRunCatching {
                qmsRepository.sendMessage(chat.id, message, files)
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
                qmsRepository.blockUser(nick)
            }.map {
                it.firstOrNull { it.user.nick == nick } != null
            }.onSuccess {
                viewState.onBlockUser(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        viewModelScope.launch {
            coRunCatching {
                qmsRepository.uploadFiles(files, pending)
            }.onSuccess {
                viewState.onUploadFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun handleEvent(event: WebSocketEvent.Qms) {
        val currentThemeId = currentData?.id?.threadId ?: return
        if (event.themeId != currentThemeId) {
            return
        }

        when (event.type) {
            is WebSocketEvent.Qms.Type.New -> {
                onNewWsMessage(event.type.messageId)
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

    private fun onNewWsMessage(messageId: QmsMessageId) {
        currentData?.let { chat ->
            val lastMessId = chat.messages.lastOrNull()?.asRegular()?.id
            viewModelScope.launch {
                coRunCatching {
                    qmsRepository.getMessagesFromWs(chat.id.threadId, messageId, lastMessId)
                }.onSuccess {
                    onNewMessages(it)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    fun checkNewMessages() {
        currentData?.let { chat ->
            val lastMessId = chat.messages.lastOrNull()?.asRegular()?.id
            viewModelScope.launch {
                coRunCatching {
                    qmsRepository.getMessagesAfter(chat.id, lastMessId)
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
        currentData?.let { chat ->
            val result = items.filter { new ->
                chat.messages.find { it.asRegular()?.id != new.asRegular()?.id } != null
            }
            val newData = chat.copy(
                messages = chat.messages + result
            )
            currentData = newData
            viewState.onNewMessages(result)
        }
    }

    fun createThemeNote() {
        currentData?.let {
            val url = "https://4pda.to/forum/index.php?act=qms&mid=${it.id.userId.id}&t=${it.id.threadId.id}"
            viewState.showCreateNote(it.title, it.user.nick, url)
        }
    }

    fun openProfile() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showuser=${it.user.id.id}")
        }
    }

    fun openDialogs() {
        currentData?.let {
            router.navigateTo(
                Screen.QmsThemes(userId = it.user.id).apply {
                    screenTitle = it.user.nick
                }
            )
        }
    }

    fun onSendClick() {
        if (currentExtra is QmsChatExtra.Existed) {
            viewState.temp_sendMessage()
        } else {
            viewState.temp_sendNewTheme()
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
