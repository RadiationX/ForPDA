package forpdateam.ru.forpda.presentation.qms.blacklist

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsBlackListPresenter(
    private val qmsInteractor: QmsInteractor,
    private val router: TabRouter,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler
) : BasePresenter<QmsBlackListView>() {


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadContacts()
    }


    fun loadContacts() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                qmsInteractor.getBlackList()
            }.onSuccess {
                viewState.showContacts(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun blockUser(nick: String) {
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.blockUser(nick)
            }.onSuccess {
                viewState.showContacts(it)
                viewState.clearNickField()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun unBlockUser(id: Int) {
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.unBlockUsers(id)
            }.onSuccess {
                viewState.showContacts(it)
                viewState.clearNickField()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun searchUser(nick: String) {
        viewModelScope.launch {
            coRunCatching {
                qmsInteractor.findUser(nick)
            }.onSuccess {
                viewState.showFoundUsers(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onItemLongClick(item: QmsContact) {
        viewState.showItemDialogMenu(item)
    }

    fun openProfile(item: QmsContact) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${item.user.id}", router)
    }

    fun openDialogs(item: QmsContact) {
        router.navigateTo(Screen.QmsThemes().apply {
            screenTitle = item.user.nick
            userId = item.user.id
            avatarUrl = item.user.avatar
        })
    }
}
