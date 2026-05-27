package forpdateam.ru.forpda.presentation.qms.themes

import android.util.Log
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.qms.QmsRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.QmsChatId
import ru.radiationx.coretypes.QmsThreadId
import ru.radiationx.coretypes.UserId
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 11.11.17.
 */
data class QmsThemesExtra(
    val userId: UserId
) : QuillExtra

@InjectViewState
class QmsThemesPresenter(
    private val argExtra: QmsThemesExtra,
    private val qmsRepository: QmsRepository,
    private val avatarRepository: AvatarRepository,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler
) : BasePresenter<QmsThemesView>() {

    var currentData: QmsThemes? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        qmsRepository
            .observeThemes(argExtra.userId)
            .filterNotNull()
            .onEach {
                currentData = it
                viewState.showThemes(it)
            }
            .launchIn(viewModelScope)

        qmsRepository
            .observeContact(argExtra.userId)
            .mapNotNull { contact ->
                contact?.user?.avatar ?: avatarRepository.getAvatar(argExtra.userId)
            }
            .onEach { viewState.showAvatar(it) }
            .launchIn(viewModelScope)
    }

    fun loadThemes() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsRepository.getThemesList(argExtra.userId)
            }.onSuccess {
                currentData = it
                if (it.themes.isEmpty()) {
                    openChat()
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
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

    fun deleteTheme(themeId: QmsThreadId) {
        currentData?.let {
            viewModelScope.launch {
                coRunCatching {
                    qmsRepository.deleteTheme(QmsChatId(it.user.id, themeId))
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    fun openProfile() {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${argExtra.userId.id}")
    }

    fun openChat() {
        currentData?.let {
            Log.e("kokosina", "openChat")
            router.replaceScreen(Screen.QmsChat.Create(userId = it.user.id))
        }
    }

    fun createNote() {
        currentData?.let {
            val url = "https://4pda.to/forum/index.php?act=qms&mid=${it.user.id.id}"
            viewState.showCreateNote(it.user.nick, url)
        }
    }

    fun createThemeNote(item: QmsTheme) {
        currentData?.let {
            val url = "https://4pda.to/forum/index.php?act=qms&mid=${it.user.id.id}&t=${item.id.id}"
            viewState.showCreateNote(item.name.orEmpty(), it.user.nick, url)
        }
    }

    fun onItemClick(item: QmsTheme) {
        currentData?.let {
            router.navigateTo(
                Screen.QmsChat.Existed(chatId = QmsChatId(it.user.id, item.id)).apply {
                    screenTitle = item.name
                    screenSubTitle = it.user.nick
                }
            )
        }
    }

    fun onItemLongClick(item: QmsTheme) {
        viewState.showItemDialogMenu(item)
    }

}
