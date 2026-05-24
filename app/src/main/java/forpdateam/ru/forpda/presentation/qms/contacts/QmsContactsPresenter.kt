package forpdateam.ru.forpda.presentation.qms.contacts

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import java.util.Locale

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsContactsPresenter(
    private val qmsInteractor: QmsInteractor,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val countersHolder: CountersHolder,
    private val errorHandler: ErrorHandler
) : BasePresenter<QmsContactsView>() {

    private val localItems = mutableListOf<QmsContact>()
    private val searchContacts = mutableListOf<QmsContact>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        qmsInteractor
            .observeContacts()
            .onEach {
                localItems.clear()
                localItems.addAll(it)
                viewState.showContacts(it)
                countersHolder.set(
                    countersHolder.get().copy(
                        qms = it.sumOf { it.count }
                    ))
            }
            .launchIn(viewModelScope)
    }

    fun searchLocal(nick: String) {
        searchContacts.clear()
        if (nick.isNotEmpty()) {
            searchContacts.filter {
                it.user.nick
                    .lowercase(Locale.getDefault())
                    .contains(nick.lowercase(Locale.getDefault()))
            }
            viewState.showContacts(searchContacts)
        } else {
            viewState.showContacts(localItems)
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsInteractor.getContactList()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun deleteDialog(id: Int) {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsInteractor.deleteDialog(id)
            }.onSuccess {
                loadContacts()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun blockUser(item: QmsContact) {
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.blockUser(item.user.nick)
            }.map {
                it.firstOrNull { it.user.nick == item.user.nick } != null
            }.onSuccess {
                viewState.onBlockUser(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onItemClick(item: QmsContact) {
        router.navigateTo(
            Screen.QmsThemes(
                userId = item.user.id,
                avatarUrl = item.user.avatar
            ).apply {
                screenTitle = item.user.nick
            }
        )
    }

    fun onItemLongClick(item: QmsContact) {
        viewState.showItemDialogMenu(item)
    }

    fun createNote(item: QmsContact) {
        val url = "https://4pda.to/forum/index.php?act=qms&mid=${item.user.id}"
        viewState.showCreateNote(item.user.nick, url)
    }

    fun openProfile(item: QmsContact) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${item.user.id}")
    }

    fun openBlackList() {
        router.navigateTo(Screen.QmsBlackList())
    }

    fun openChatCreator() {
        router.navigateTo(Screen.QmsChat.Create(link))
    }
}
